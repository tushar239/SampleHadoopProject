package filesystem;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

import java.io.InputStream;
import java.net.URI;

/*
 I have Cloudera VM on VMWare Fusion. This VM has an IP address 192.168.31.131.
 When you connect to this VM using its IP address "hdfs://192.168.31.131/...", it will read /etc/hadoop/conf/core-site.xml's

  <property>
    <name>fs.defaultFS</name>
    <value>hdfs://quickstart.cloudera:8020</value>  This is the default url how hdfs is accessed on that VM
  </property>

 and access hdfs.

 copy input.txt from your local machine to vm.
    scp input.txt cloudera@<vm ip address>:<some place in vm's file system>
 To put this file in HDFS of VM, connect to VM using ssh cloudera@192.168.31.131  (passowrd=cloudera)
    hadoop fs -put <file in vm machine that you copied from your local machine>
    This file will be put at some location like hdfs://quickstart.cloudera:8020/user/cloudera/<file>
 To see this file in HDFS from VM
    hadoop fs -ls /user/cloudera
    is same as
    hadoop fs -ls hdfs://localhost/user/cloudera
    is same as
    hadoop fs -ls hdfs://quickstart.cloudera:8020/user/cloudera

 To access VM's HDFS from your local machine
    hadoop fs -ls hdfs://192.168.31.131/user/cloudera
*/
public class HdfsAccessOnDistributedEnv {

    public static void main(String[] args) throws Exception {
        String ipAddressOfHadoopVM = "192.168.31.131";
        String uri = "hdfs://" + ipAddressOfHadoopVM + "/user/cloudera/input";
        // this configuration can be modified with properties that you mention in hadoop's conf files like core-site.xml, hdfs-site.xml etc. Default configuration will be read from these files, but you can override them here, if you want.
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(URI.create(uri), conf);
        InputStream in = null;
        try {
            // This Path object is not from file.io. It is from org.apache.hadoop.fs.
            in = fs.open(new Path(uri));
            IOUtils.copyBytes(in, System.out, 4096, false);
        } finally {
            IOUtils.closeStream(in);
        }
    }

    /*
    O/P:
Hi,How,are,you,How,is,your,family,How,are,you,feeling,today,Wish,you,good,luck
Hi,How,are,you,How,is,your,family,How,are,you,feeling,today,Wish,you,good,luck
Hi,How,are,you,How,is,your,family,How,are,you,feeling,today,Wish,you,good,luck
     */
}
