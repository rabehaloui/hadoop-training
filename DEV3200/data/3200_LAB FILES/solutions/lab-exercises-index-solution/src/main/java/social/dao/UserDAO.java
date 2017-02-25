package social.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;

import social.model.User;

public class UserDAO {

    public static final String userdirectory = ".";
  //  public static final String userdirectory = System.getProperty("user.home");
    public static final String tableName = userdirectory + "/user";
    public static final byte[] TABLE_NAME = Bytes.toBytes(tableName);
    public static final byte[] DATA_CF = Bytes.toBytes("data");
    // columns: firstName; lastName
    public static byte[] FIRSTNAME_COL = Bytes.toBytes("firstname");
    public static byte[] LASTNAME_COL = Bytes.toBytes("lastname");
    //   private static final Logger log = Logger.getLogger(UserDAO.class);
    private Configuration conf = null;
    private HTableInterface tableInterface = null;

	public UserDAO(Configuration conf) throws IOException{
		this.conf=conf;
	}

    public UserDAO(HTableInterface tableInterface) {
        this.tableInterface = tableInterface;
    }

    private static Get makeGet(String user) throws IOException {
        System.out.println("make get  [" + user + "]");
        Get g = new Get(Bytes.toBytes(user));
        g.addFamily(DATA_CF);
        return g;
    }

    private static Put makePut(User u) {
        System.out.println("make put  [" + u + "]");
        Put p = new Put(Bytes.toBytes(u.userId)); //
        p.add(DATA_CF, FIRSTNAME_COL, Bytes.toBytes(u.firstName));
        p.add(DATA_CF, LASTNAME_COL, Bytes.toBytes(u.lastName));
        return p;
    }

    public static Put makePut(String userId, byte[] fam, byte[] qual, byte[] val) {
        Put p = new Put(Bytes.toBytes(userId));
        p.add(fam, qual, val);
        return p;
    }

    private static Delete makeDel(String userId) {
        System.out.println(String.format("make Delete for %s", userId));
        Delete d = new Delete(Bytes.toBytes(userId));
        return d;
    }

    private static Scan makeScan() {
        Scan s = new Scan();
        s.addFamily(DATA_CF);
        return s;
    }

    public void addUser(String userId, String firstName, String lastName) throws IOException {
        HTableInterface table = getTable();
        Put p = makePut(new User(userId, firstName, lastName));
        table.put(p);
        table.close();
    }

    public User getUser(String userId)
            throws IOException {
        HTableInterface table = getTable();
        Get g = makeGet(userId);
        Result result = table.get(g);
        if (result.isEmpty()) {
            System.out.println(String.format("user %s not found.", userId));
            return null;
        }
        User s = createUser(result);
        table.close();
        return s;
    }

    public void deleteUser(String userId) throws IOException {
        HTableInterface table = getTable();
        Delete d = makeDel(userId);
        table.delete(d);
        table.close();
    }

    public List<User> getUsers()
            throws IOException {
        HTableInterface table = getTable();
        ResultScanner results = table.getScanner(makeScan());
        ArrayList<User> ret = new ArrayList<User>();
        for (Result r : results) {
            ret.add(createUser(r));
        }
        table.close();
        return ret;
    }

    private User createUser(Result r) {
        return createUser(r.getRow(), r.getValue(DATA_CF, FIRSTNAME_COL), r.getValue(DATA_CF, LASTNAME_COL));
    }

    private User createUser(byte[] userId, byte[] firstName, byte[] lastName) {
        return new User(Bytes.toString(userId), Bytes.toString(firstName), Bytes.toString(lastName));
    }

    public HTableInterface getTable() throws IOException{
        HTableInterface table;
        if (conf != null) {
            table  = new HTable(conf, TABLE_NAME);
        } else {
            table = tableInterface;
        }
        return table;
    }
}