package filesystem;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;

import java.net.URI;

/**
 * @author Tushar Chokshi @ 10/30/17.
 */
public class ListHdfsFiles {
    // this is same as command 'hadoop fs -ls hdfs://192.168.31.131/<default dir is /user/<username> >
    public static void main(String[] args) throws Exception {
        String uri = "hdfs://192.168.31.131";
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(URI.create(uri), conf);

        //Path path = new Path(uri + "/user/cloudera/");
        //or
        Path path = new Path(uri); // default directory to access in hdfs will be /user/<username with which linux os is booted on namenode>
        FileStatus[] status = fs.listStatus(path);
        Path[] listedPaths = FileUtil.stat2Paths(status);
        for (Path p : listedPaths) {
            System.out.println(p);
        }
    }

    /*
    O/P:
hdfs://192.168.31.131/user/cloudera/input
hdfs://192.168.31.131/user/cloudera/output
hdfs://192.168.31.131/user/cloudera/output1
     */
}
