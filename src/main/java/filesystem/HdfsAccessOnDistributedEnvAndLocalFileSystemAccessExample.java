package filesystem;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

import java.io.InputStream;
import java.net.URI;

/*

This program is a java code for 'hadoop fs -ls hdfs://<namenode ip address>/user/cloudera/input'
Just like that you can write java code for many hdfs commands. You can read book's pg 56-68.

If you have a client node in the cluster, I think you can connect to client node instead of namenode directly.


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
 If you are inside hadoop cluster's node
    hadoop fs -ls <default dir is /user/<username> >
    is same as
    hadoop fs -ls /user/cloudera
    is same as
    hadoop fs -ls hdfs://localhost/user/cloudera
    is same as
    hadoop fs -ls hdfs://quickstart.cloudera:8020/user/cloudera

 If you are outside hadoop cluster, then to access HDFS
    hadoop fs -ls hdfs://192.168.31.131/user/cloudera, where 192.168.31.131 is namenode's ip address


Anatomy of a File Read (pg 69 of Book).

    you can see the book for diagram flow.

    How read operation on HDFS works?

    - Client opens a file using fileSystem.open(file uri). In case of hdfs:// scheme, file system will be of type DistributedFileSystem.
    - Object of DistributedFileSystem will make a call to NameNode. NameNode will return addresses of few DataNodes containing first few blocks of the file.
    - These DataNodes will be sorted as per proximity from the client.
    - The DistributedFileSystem returns an FSDataInputStream (an input stream that supports file seeks) to the client for it to read data from. FSDataInputStream in turn wraps a DFSInputStream, which manages the datanode and namenode I/O.
    - The client then calls read() on the stream. It will be connected to the first DataNode the sorted list.
    - Data is streamed from the datanode back to the client, which calls read() repeatedly on the stream.
    - When the end of the block is reached, DFSInputStream will close the connection to the datanode, then find the best datanode for the next block.
      This happens transparently to the client, which from its point of view is just reading a continuous stream.
    - Blocks are read in order, with the DFSInputStream opening new connections to datanodes as the client reads through the stream.
    - It will also call the namenode to retrieve the datanode locations for the next batch of blocks as needed.
    - When the client has finished reading, it calls close() on the FSDataInputStream.

    (IMP)
    While reading a file,
    DFSInputStream, which has stored the datanode addresses for the first few blocks in the file, then connects to the first (CLOSEST) datanode for the first block in the file.
    When the end of the block is reached, DFSInputStream will close the connection to the datanode, then find the best datanode for the next block

    How does it determine the Closest DataNode? (pg 71)
        If block of the file is on the node where the process runs, then that's the closest one
        If not, then it will try a node on the same rack.
        If not, then it will try a node in different data center.



Anatomy of File Write (pg 72 of Book).

    you can see the book for diagram flow.

    - Just like File Read, during write, DistributedFileSystem contacts NameNode to register a file in it and it returns FSDataOutputStream to the client.
    - Client starts writing data to this OutputStream. Data is put in Data Queue, which is read by DataStream.
    - DataStream contacts NameNode to provide DataNode where block can be written.
    - It writes a block to that DataNode.
    - That DataNode forwards that block through Ack Queue to another DataNode for replication. If replication fails, data is put back into the Ack Queue and retried.

    How the blocks are replicated in the cluster? (pg 74 of Book)
        One copy is kept on the same node where the original block is stored.
        Second copy is kept on a node of different Rack.
        Third copy is kept on another node of that Rack.




*/
public class HdfsAccessOnDistributedEnvAndLocalFileSystemAccessExample {

    public static void main(String[] args) throws Exception {
        // Accessing HDFS (Distributed File System)
        {
            String ipAddressOfHadoopVM = "192.168.31.131";
            String uri = "hdfs://" + ipAddressOfHadoopVM + "/user/cloudera/input";
            // this configuration can be modified with properties that you mention in hadoop's conf files like core-site.xml, hdfs-site.xml etc. Default configuration will be read from these files, but you can override them here, if you want.
            Configuration conf = new Configuration();
            FileSystem fs = FileSystem.get(URI.create(uri), conf); // This will return an object of DistributedFileSystem because uri has hdfs:// scheme in it. If you use s3:// scheme, it will return S3FileSystem. If you use file:// scheme, it will return LocalFileSystem. If you use ftp:// scheme, it will return FtpFileSystem and so on.
            InputStream in = null;
            try {
                // This Path object is not from file.io. It is from org.apache.hadoop.fs.
                in = fs.open(new Path(uri)); // This returns FSDataInputStream that wraps DFSInputStream. Client starts reading the blocks from this InputStream.
                IOUtils.copyBytes(in, System.out, 4096, false);
            } finally {
                IOUtils.closeStream(in);
            }
        }

        // Accessing Local File System. When you run this code on a node of a cluster, it can access that node's local file (just like normal file in normal file system), not HDFS file.
        {
            String uri = "file:///Users/chokst/Desktop/Temp/Temp.txt";
            Configuration conf = new Configuration();
            LocalFileSystem fs = FileSystem.getLocal(conf);
            InputStream in = null;
            try {
                // This Path object is not from file.io. It is from org.apache.hadoop.fs.
                in = fs.open(new Path(uri));
                IOUtils.copyBytes(in, System.out, 4096, false);
            } finally {
                IOUtils.closeStream(in);
            }
        }

        // Writing a file to DistributedFileSystem (HDFS). This is similar to 'hadoop fs -put' command.
        // pg. 74 of a book
/*
        {
            Path p = new Path("p");
            DistributedFileSystem fs = new DistributedFileSystem();
            FSDataOutputStream out = fs.create(p);
            out.write("content".getBytes("UTF-8"));
            out.hflush();
            //assertThat(fs.getFileStatus(p).getLen(), is(((long) "content".length())));
        }
*/
    }

    /*
    O/P:
    Hi,How,are,you,How,is,your,family,How,are,you,feeling,today,Wish,you,good,luck
    Hi,How,are,you,How,is,your,family,How,are,you,feeling,today,Wish,you,good,luck
    Hi,How,are,you,How,is,your,family,How,are,you,feeling,today,Wish,you,good,luck
     */
}
