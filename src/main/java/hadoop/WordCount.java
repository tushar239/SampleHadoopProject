package hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.LazyOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;

/*

When you read this open, a book as well as 'MapReduce Document Self-Prepared.docx'

For Hadoop1 vs Hadoop2, read 'Hadoop1 and Hadoop2 Self Created Document.docx'
For Hadoop Configuration, read 'Hadoop Configuration Self-Created Document.txt'
For using Cloudera Hadoop VM, read 'Running WordCount job on Cloudera VM.txt'


Hadoop File System  (pg 53)

    Hadoop can work with many file systems, not just HDFS. HDFS is a preferred one because of its unique feature of creating large blocks.
    Hadoop can work
    - Local FS
    - HDFS
    - WebHDFS
    - HAR
    - View
    - FTP
    - S3   ---- AWS EMR allows you to use S3 instead of HDFS.
    - Azure
    - Swift

    Hadoop has an abstract notion of filesystems, of which HDFS is just one implementation.
    The Java abstract class org.apache.hadoop.fs.FileSystem represents the client interface to a filesystem in Hadoop, and there are several concrete implementations.

    https://tutorials.techmytalk.com/2014/08/16/hadoop-hdfs-java-api/

    Hadoop’s org.apache.hadoop.fs.FileSystem is generic class to access and manage HDFS files/directories located in distributed environment. File’s content stored inside datanode with multiple equal large sizes of blocks (e.g. 128 MB), and namenode keep the information of those blocks and Meta information. FileSystem read and stream by accessing blocks in sequence order. FileSystem first get blocks information from NameNode then open, read and close one by one. It opens first blocks once it complete then close and open next block. HDFS replicate the block to give higher reliability and scalability and if client is one of the datanode then it tries to access block locally if fail then move to other cluster datanode.
    FileSystem uses FSDataOutputStream and FSDataInputStream to write and read the contents in stream. Hadoop has provided various implementation of FileSystem as described below:

    1) DistributedFileSystem: To read/write a file in HDFS (Hadoop Distributed File System)
    2) LocalFileSystem: To read/write a file in Local File System
    3) FTPFileSystem: To read/write a file using FTP client
    4) S3FileSystem: To read/write a file in S3
    5) WebHdfsFileSystem: To read/write a file over the web
    and a few more

    'hadoop fs' command is used to read/write file from these file systems.
    'hadoop fs -put <local file system location> <hdfs://localhost:9000/.....> This says that you want to put a file from node's local file system to HDFS.
    'hadoop ps -get <hdfs://localhost:9000/.....> <local file system location>  This says that you want to copy a file from HDFS to node's local file syst\m.
    if you use file:// scheme instead of hdfs://, it will use local file system
    if you use s3:// scheme instead of hdfs://, it will use s3 file system

    You can see above url to see how to write Java code to access HDFS in different File Systems.
    Similar information is there in a book on pg 56
    Read HdfsAccessOnDistributedEnvAndLocalFileSystemAccessExample.java

HDFS (Hadoop Distributed File System)

    HDFS is built around the idea that the most efficient data processing pattern is a write-once, read-many-times pattern.

    When you put a file in HDFS, it is divided into blocks. Each bock has a default size of 128 MB.
    Each block is stored on different DataNode and its replica are stored on other DataNodes.
    Default size of InputSplit for a mapper is the size of HDFS block. Later, you will see the disadv of keeping it different than block size.

    Formula to decide split size=max(mapred.min.split.size, min(mapred.max.split.size, dfs.block.size)).
    So, you can control split size using mapred.min.split.size.

    Filesystem blocks are typically a few kilobytes in size, whereas disk blocks are normally 512 bytes.
    HDFS, too, has the concept of a block, but it is a much larger unit—128 MB by default.
    (IMP) Unlike a filesystem for a single disk, a file in HDFS that is smaller than a single block does not occupy a full block’s worth of under‐ lying storage. (For example, a 1 MB file stored with a block size of 128 MB uses 1 MB of disk space, not 128 MB.)

    (IMP) Why Is a Block in HDFS So Large?
    HDFS blocks are large compared to disk blocks, and the reason is to minimize the cost of seeks. If the block is large enough, the time it takes to transfer the data from the disk can be significantly longer than the time to seek to the start of the block. Thus, trans‐ ferring a large file made of multiple blocks operates at the disk transfer rate.
    A quick calculation shows that if the seek time is around 10 ms and the transfer rate is 100 MB/s, to make the seek time 1% of the transfer time, we need to make the block size around 100 MB. The default is actually 128 MB, although many HDFS installations use larger block sizes. This figure will continue to be revised upward as transfer speeds grow with new generations of disk drives.
    This argument shouldn’t be taken too far, however. Map tasks in MapReduce normally operate on one block at a time, so if you have too few tasks (fewer than nodes in the cluster), your jobs will run slower than they could otherwise.

    (IMP) Hadoop works better with a small number of large files than a large number of small files.

    Like its disk filesystem cousin, HDFS’s fsck command understands blocks. For example, running:
        % hdfs fsck / -files -blocks
    will list the blocks that make up each file in the filesystem.

    Seek Time is measured defines the amount of time it takes a hard drive's read/write head to find the physical location
    of a piece of data on the disk.
    The disk transfer rate (sometimes called media rate) is the speed at which data is transferred to and from the disk media
    (actual disk platter) and is a function of the recording frequency. It is generally described in megabytes per second (MBps).

    NameNode, ClientNode, DataNode, SecondaryNameNode
        NameNode:
            NameNode is a master node that keeps meta data of blocks stored in DataNodes.
            Secondary NameNode is a backup for NameNode. It doesn't start working until primary NameNode goes down.
            This is to avoid SPOF of NameNode.

            NameNode manages the File System's namespace/meta-data/file blocks
            - Runs on 1 machine to several machines
            - It maintains 'Namespace Image(fsimage)' and 'Edit Logs'.
            Namespace Image has all metadata like block information, permissions, data node information etc.
            Edit Logs has all activities.  (VERY IMPORTANT)
            It is recommended to have 1GB main memory in NameNode for million storage blocks.

        ClientNode:
            A client accesses the filesystem on behalf of the user by communicating with the name‐ node and datanodes. The client presents a filesystem interface similar to a Portable Operating System Interface (POSIX), so the user code does not need to know about the namenode and datanodes to function.

        DataNode:
            Datanodes are the workhorses of the filesystem. They store and retrieve blocks when they are told to (by clients or the namenode), and they report back to the namenode periodically with lists of blocks that they are storing.

        NameNode Failover (High Availability of NameNode):

            Without the namenode, the filesystem cannot be used. In fact, if the machine running the namenode were obliterated, all the files on the filesystem would be lost since there would be no way of knowing how to reconstruct the files from the blocks on the datanodes.
            For this reason, it is important to make the namenode resilient to failure, and Hadoop provides two mechanisms for this.

                1) The first way is to back up the files that make up the persistent state of the filesystem metadata.
                Hadoop can be configured so that the namenode writes its persistent state to multiple filesystems. These writes are synchronous and atomic.
                The usual configuration choice is to write to local disk as well as a remote NFS mount.

                2) It is also possible to run a secondary namenode, which despite its name does not act as a namenode.
                (IMP) Its main role is to periodically merge the namespace image with the edit log to prevent the edit log from becoming too large.
                The secondary namenode usually runs on a separate physical machine because it requires plenty of CPU and as much memory as the namenode to perform the merge.
                It keeps a copy of the merged name‐space image, which can be used in the event of the namenode failing. However, the state of the secondary namenode lags that of the primary, so in the event of total failure of the primary, data loss is almost certain.
                The usual course of action in this case is to copy the namenode’s metadata files that are on NFS to the secondary and run it as the new primary.

            Hadoop 2 remedied this situation by adding support for HDFS high availability (HA). It requires following configurations.

                1) Shared Edit Logs:
                You can have one active namenode and multiple standby namenodes. You need to keep common storage between these namenodes for storing Edit Logs.
                When new entry is added in Edit Log, all namenodes will update their Namespace Image.
                For this shared edit logs, there are two solutions:
                    - NFS filer
                    - quorum journal manager (QJM).
                    The QJM is a dedicated HDFS implementation, designed for the sole purpose of providing a highly available edit log, and is the recommended choice for most HDFS installations.
                    The QJM runs as a group of journal nodes, and each edit must be written to a majority of the journal nodes.
                    Typically, there are three journal nodes, so the system can tolerate the loss of one of them.
                    This arrangement is similar to the way ZooKeeper works, although it is important to realize that the QJM implementation does not use ZooKeeper.
                    (Note, however, that HDFS HA does use ZooKeeper for electing the active namenode, as explained in the next section.)
                2) Datanodes must send block reports to both namenodes because the block mappings are stored in a namenode’s memory, and not on disk.
                3) Clients must be configured to handle namenode failover, using a mechanism that is transparent to users.
                4) The secondary namenode’s role is subsumed by the standby, which takes periodic checkpoints of the active namenode’s namespace.

                If the active namenode fails, the standby can take over very quickly (in a few tens of seconds)

           Failover Controller and Fencing

                Failover Controller:

                    Zookeeper is used as Failover Controller to elect a new primary namenode in case of current primary namenode fails.
                    graceful failover - Failover may also be initiated manually by an administrator for some routing maintenance.
                    ungraceful failover - primary namenode goes down due to some reason.
                    Both the cases are handled very well by Zookeeper.

                Fencing:

                    Sometimes, it is impossible to be sure that the failed namenode has stopped running. For example, a slow network or a network partition can trigger a failover transition.
                    Even though the previously active namenode is still running and thinks it is still the active namenode.
                    The HA implementation goes to great lengths to ensure that the previously active namenode is prevented from doing any damage and causing corruption—a method known as fencing.

        ClientNode Failure:

            In you application, you can configure list of all namenodes. This should be used as a failover when clientnode goes down. Your application can directly connect to one of the available namenodes instead of going through clientnode.

    HDFS Federation:

        The namenode keeps a reference to every file and block in the filesystem in memory, which means that on very large clusters with many files, memory becomes the limiting factor for scaling HDFS federation, introduced in the 2.x release series, allows a cluster to scale by adding namenodes, each of which manages a portion of the filesystem namespace.
        For example, one namenode might manage all the files rooted under /user, say, and a second name‐ node might handle files under /share.

    Local File System vs Hadoop File System (HDFS):
    (file:// scheme vs hdfs:// scheme for commands)

        https://www.youtube.com/watch?v=D0n90c4cjD8

        By connecting to a node, you can access its local file system using normal
        Linux commands like ls, cat, vim etc.
            or using file scheme with 'hadoop fs' command
        hadoop fs -ls file:///.../...

        Remember, you are not accessing HDFS files using file:/// scheme. You are accessing Local File System.

        To access HDFS, you need to use hdfs scheme

            hadoop fs -ls hdfs://namenodehost/<default dir is /user/<username> >

            default hdfs directory that will be accessed will be /user/<username with which OS is booted>, but you can provide any dir name
            namenode host will be localhost, if you are connecting to namenode machine to run this command.
            it will be namenode's ip address, if you are connecting from outside hadoop cluster.
            this host name will converted to HDFS's url by looking into core-site.xml's fs.defaultFS configuration.

            If you have a client node in the cluster, I think you can connect to client node instead of namenode directly.

            If you are in the node of a hadoop cluster,
                You can also use
                    hadoop fs -ls <default dir is /user/<username> >
                    or
                    hadoop fs -ls hdfs://localhost/<default dir is /user/<username> >   (hdfs://localhost/ will not work. it will connect to local file system)
                It will be converted to
                    hadoop fs -ls hdfs://<core-site.xml's namenode host:port>/<default dir is /user/<username> >

           From outside hadoop cluster,
                You can use
                    hadoop fs -ls hdfs://<ip address of namenode>/<default dir is /user/<username> >


    HDFS commandline commands:

        https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-common/FileSystemShell.html

        https://www.youtube.com/watch?v=oBqju4ZkD58

        The FS shell is invoked by:
        bin/hadoop fs <args>
        or
        hdfs dfs <args>

        look at -copyFromLocal, -copyToLocal, -mkdir, -ls, -appendToFile, -put, -get commands. They are frequently used.

        You can create a directory in hdfs using
        hadoop fs -mkdir ...
        but you cannot create a file in hdfs directly. you need to create it in local file system and put it in hdfs.


        hadoop fs -copyFromLocal input/docs/input.txt hdfs://localhost/user/tom/input.txt

        In fact, we could have omitted the scheme and host of the URI and picked up the default, hdfs://localhost, as specified in core-site.xml:

        hadoop fs -copyFromLocal input/docs/input.txt /user/tom/input.txt

        If you use

        hadoop fs -copyFromLocal input/docs/input.txt file:///.../...
        then it will copy the file in local file system of the node where you are running this command. It is not copied in HDFS.

        If there fs.defaultFS is set in core-site.xml,

            hadoop fs -copyFromLocal input/docs/input.txt hdfs://localhost:9000/user/tom/input.txt
            is same as
            hadoop fs -appendToFile input/docs/input.txt /user/tom/input.txt   ----- hdfs://localhost:9000 is appended implicitly.

            hadoop fs -put <from location from your local machine> <to location in hdfs> ----- this will ask namenode to put a file into data nodes. namenode will chunk this file into blocks and then put it into datanodes.


            hadoop fs -copyFromLocal ...    ---- same as -put command
            hadoop fs -moveFromLocal ...    ---- cut and past
            If source is a file then destination can be a file or directory
            If source is a directory then destination has to be a directory

            hadoop fs -get <hdfs location of file>  <local filesystem location>
            or
            hdfs dfs -get /user/cloudera/file <local filesystem location>  ---- copies a file from hdfs to local filesystem. hdfs://localhost:9000 is prefixed implicitly to /user/cloudera/file
            or
            hdfs dfs -get hdfs://namenodehost:port/user/cloudera/file <local filesystem location>

            hadoop fs -copyToLocal ...
            hadoop fs -moveToLocal ...

        core-site.xml

            <property>
                <name>fs.defaultFS</name> fs.default.name is deprecated
                <value>hdfs://localhost:9000</value> <!-- NameNode URI. This is a URI using which HDFS files can be located-->
            </property>

            fs.defaultFS Specifies the NameNode and the default file system, in the form
            hdfs://<namenode host>:<namenode port>/.
            The default value is file///.
            The default file system is used to resolve relative paths
            for example, if fs.defaultFS is set to hdfs://mynamenode/, the relative URI /mydir/myfile resolves to hdfs://mynamenode/mydir/myfile.

            If you want to access the normal file system on NameNode
            hadoop fs -ls file:/// --- it will list all folders and files under root node (not sure though ???????)


        hdfs-site.xml  --- all hdfs related configuration

            <configuration>
                <property>
                    <name>dfs.data.dir</name> --- hdfs datanode data will be stored at this location
                    <value>/usr/hadoop/dfs/data/</value>  ----- should have chmod 755 permissions to data dir. 777 permissions won't work. http://supunk.blogspot.com/2013/04/starting-hadoop-setting-datanode.html
                </property>
                <property>
                    <name>dfs.name.dir</name> --- hdfs namenode data (editlogs) will be stored at this location
                    <value>/usr/hadoop/dfs/name/</value>  ----- chmod 777 permissions will work for name node
                </property>
                <property>
                    <name>dfs.replication</name>
                    <value>1</value>
                </property>
            </configuration>

        mapred-site.xml --- all mapred related configurations

            <property>
                <name>mapred.job.tracker</name>
                <value>localhost:9001</value>
            </property>



    AWS EMR (Elastic MapReduce) Service:

        At this point in time, AWS EMR (Elastic MapReduce) Service doesn’t provide facility of NameNode failover.
        If the master node goes down, your cluster will be terminated and you’ll have to rerun your job. Amazon EMR currently does not support automatic failover of the
         master nodes or master node state recovery. In case of master node failure, the AWS Management console displays “The master node was terminated” message
        which is an indicator for you to start a new cluster. Customers can instrument check pointing in their clusters to save intermediate data (data created in the middle of
        a cluster that has not yet been reduced) on Amazon S3. This will allow resuming the cluster from the last check point in case of failure.

    Anatomy of File Read and Write:
        Read HdfsAccessOnDistributedEnvAndLocalFileSystemAccessExample.java

    How DataNodes are chosen for replication? (pg 74)

        See diagram on pg 74 of Book.

        Hadoop’s default strategy is to place the first replica on the same node.  (IMP - it writes first replica on the same now where original block resides)
        The second replica is placed on a different rack from the first (off-rack), chosen at random.
        The third replica is placed on the same rack as the second, but on a different node chosen at random.

    Parallel file/dir copying process using distcp command (pg 76)

        hadoop distcp file1 file2
        hadoop distcp dir1 dir2
        hadoop distcp -update dir1 dir2

        distcp is implemented as a MapReduce job where the work of copying is done by the maps that run in parallel across the cluster. There are no reducers. Each file is copied by a single map, and distcp tries to give each map approximately the same amount of data by bucketing files into roughly equal allocations.

        A very common use case for distcp is for transferring data between two HDFS clusters.
        hadoop distcp -update -delete -p hdfs://namenode1/foo hdfs://namenode2/foo

        By default, up to 20 mappers are used, but this can be changed by specifying the -m argument to distcp.

        Keeping HDFS cluster in balance:
            For example, if you specified -m 1, a single map would do the copy, which apart from being slow and not using the cluster resources efficiently— would mean that the first replica of each block would reside on the node running the map (until the disk filled up). The second and third replicas would be spread across the cluster, but this one node would be unbalanced.
            By having more maps than nodes in the cluster, this problem is avoided. For this reason, it’s best to start by running distcp with the default of 20 maps per node.

            However, it’s not always possible to prevent a cluster from becoming unbalanced. Perhaps you want to limit the number of maps so that some of the nodes can be used by other jobs.
            In this case, you can use the balancer tool to subsequently even out the block distribution across the cluster.
MapReduce


   Data Locality in Hadoop

        https://data-flair.training/blogs/data-locality-in-hadoop-mapreduce/

        In Hadoop, datasets are stored in HDFS. Datasets are divided into blocks and stored across the datanodes in Hadoop cluster. When a user runs the MapReduce job then NameNode sent this MapReduce code to the datanodes on which data is available related to MapReduce job.

        Categories of Data locality in Hadoop
        - Data local data locality in Hadoop
          When the data is located on the same node as the mapper working on the data it is known as data local data locality. In this case, the proximity of data is very near to computation. This is the most preferred scenario.
        - Intra-Rack data locality in Hadoop
          It is not always possible to execute the mapper on the same datanode due to resource constraints. In such case, it is preferred to run the mapper on the different node but on the same rack.
        - Inter-Rack data locality in Hadoop
          Sometimes it is not possible to execute mapper on a different node in the same rack due to resource constraints. In such case, we will execute the mapper on the nodes on different racks. This is the least preferred scenario.

        How to optimize data locality?
            Although Data locality is the main advantage of Hadoop MapReduce as map code is executed on the same datanode where data resides.
            But this is not always true in practice due to various reasons like speculative execution in Hadoop, Heterogeneous cluster, Data distribution and placement, and Data Layout and Input Splitter.

            Challenges become more prevalent in large clusters, because more the number of data nodes and data, less will be the locality.
            In larger clusters, some nodes are newer and faster than the other, creating the data to compute ratio out of balance thus, large clusters tend not be completely homogeneous.
            In speculative execution even though the data might not be local, but it uses the computing power.
            The root cause also lies in the data layout/placement and the used Input Splitter.
            Non-local data processing puts a strain on the network which creates problem to scalability.
            Thus the network becomes the bottleneck.

            We can improve data locality by first detecting which jobs has the data locality problem or degrade over time.
            Problem-solving is more complex and involves changing the data placement and data layout, using a different scheduler or by simply changing the number of mapper and reducer slots for a job.

    YARN (Yet Another Resource Negotiator) (pg 79)

        Read "YARN (MapReduce2).docx"

    MapReduce has two important programs - Job Tracker and Task tracker.
    Job Tracker is on master node (can be different than HDFS' NameNode).
    Task Tracker runs on every single DataNode of HDFS.

                                                Traditional RDBMS				MapReduce

        Data size Access Updates Transactions	Gigabytes						Petabytes
        Access									Interactive and batch			Batch
        Updates									Read and write many times ACID	Write once, read many times
        Transactions							ACID							None
        Structure								Schema-on-write					Schema-on-read
        Integrity								High							Low
        Scaling									Nonlinear						Linear

        However, the differences between relational databases and Hadoop systems are blurring.
        Relational databases have started incorporating some of the ideas from Hadoop, and from the other direction, Hadoop systems such as Hive are becoming more interactive (by moving away from MapReduce) and adding features like indexes and transactions that make them look more and more like traditional RDBMSs.

        Hadoop works well on unstructured or semi-structured data because it is designed to interpret the data at processing time (so called schema-on-read).

        Data Locality:
        Hadoop tries to co-locate the data with the compute nodes, so data access is fast because it is local.6 This feature, known as data locality, is at the heart of data processing in Hadoop and is the reason for its good performance.
        MapReduce function runs on the nodes where data resides. This avoids network latency for function to read data for processing.

        Shared-nothing architecture:
        Coordinating the processes in a large-scale distributed computation is a challenge.
        The hardest aspect is gracefully handling partial failure—when you don’t know whether or not a remote process has failed—and still making progress with the overall computation.
        Distributed processing frameworks like MapReduce spare the programmer from having to think about failure, since the implementation detects failed tasks and reschedules replacements on machines that are healthy.
        MapReduce is able to do this because it is a shared-nothing architecture, meaning that tasks have no dependence on one other. (This is a slight oversimplification, since the output from mappers is fed to the reducers, but this is under the control of the MapReduce system; in this case, it needs to take more care rerunning a failed reducer than rerunning a failed map, because it has to make sure it can retrieve the necessary map outputs and, if not, regenerate them by running the relevant maps again.)

        Example of MapReduce data structure:
        pg 23
        Mapper is given input data in the format of key-value (offset of line-line text).
        Mapper extracts necessary information from the input and creates key-value pairs.
        Before sending this output of mappers to reducer(s), the are sorted and grouped by keys (Shuffling).

        e.g. input to Mapper
            (0, 0067011990999991950051507004...9999999N9+00001+99999999999...)
            (106, 0043011990999991950051512004...9999999N9+00221+99999999999...)
            (212, 0043011990999991950051518004...9999999N9-00111+99999999999...)
            (318, 0043012650999991949032412004...0500001N9+01111+99999999999...)

        Output of Mapper
            year-temperature pairs

            (1950, 0)
            (1950, 22)
            (1950, −11)
            (1949, 111)
            (1949, 78)

        Sorted by keys and grouped
            (1949, [111, 78])
            (1950, [0, 22, −11])

        Output of Reducer
            Max temperature in a year
            (1949, 111)
            (1950, 22)

        See Mapper example on pg 24

        Rather than using built-in Java types, Hadoop provides its own set of basic types that are optimized for network serialization.
        These are found in the org.apache.hadoop.io package. Here we use LongWritable, which corresponds to a Java Long, Text (like Java String), and IntWritable (like Java Integer).

        See Reducer example on pg 26

    The input types of the reduce function must match the output types of the map function: Text and IntWritable.

     pg 23-27

     Rather than using built-in Java types, Hadoop provides its own set of basic types that are op‐ timized for network serialization. These are found in the org.apache.hadoop.io pack‐ age. Here we use LongWritable, which corresponds to a Java Long, Text (like Java String), and IntWritable (like Java Integer).

     The input types of the reduce function must match the output types of the map function: Text and IntWritable.

     static addInputPath() method on FileInputFormat, and it can be a single file, a directory (in which case, the input forms all the files in that direc‐ tory), or a file pattern. As the name suggests, addInputPath() can be called more than once to use input from multiple paths.

     The setOutputKeyClass() and setOutputValueClass() methods control the output types for the reduce function, and must match what the Reduce class produces. The map output types default to the same types, so they do not need to be set if the mapper produces the same types as the reducer (as it does in our case). However, if they are different, the map output types must be set using the setMapOutputKeyClass() and setMapOutputValueClass() methods.

     The waitForCompletion() method on Job submits the job and waits for it to finish. The single argument to the method is a flag indicating whether verbose output is generated. When true, the job writes information about its progress to the console.
     The return value of the waitForCompletion() method is a Boolean indicating success (true) or failure (false), which we translate into the program’s exit code of 0 or 1.

     Commands to run this job
        hadoop jar ./target/SampleHadoopProject-1.0-SNAPSHOT.jar hadoop.WordCount ./input ./output

        On actual hadoop cluster, input and output files are kept in HDFS.

     You can run the this main class from IntelliJ also.

     Somehow, I could not make hadoop work properly in my local mac. So, I used 'Cloudera Hadoop VM' on 'VMWare Fusion for Mac'.
     Cloudera Hadoop VM has all installed and configured for Hadoop and and its Ecosystem libraries and tools.
     You can read 'Running WordCount job on Cloudera VM.txt'

     When you run the job, you see some important information about the job.

        For example, we can see that the job was given an ID of job_local26392882_0001, and it ran one map task and one reduce task (with the following IDs: attempt_local26392882_0001_m_000000_0 and attempt_local26392882_0001_r_000000_0).
        Knowing the job and task IDs can be very useful when debugging MapReduce jobs.

        The last section of the output, titled “Counters,” shows the statistics that Hadoop generates for each job it runs.

        These are very useful for checking whether the amount of data processed is what you expected. For example, we can follow the number of records that went through the system: five map input records produced five map output records (since the mapper emitted one output record for each valid input record), then five reduce input records in two groups (one for each unique key) produced two reduce output records.

        The output was written to the output directory, which contains one output file per reducer.
        The job had a single reducer, so we find a single file, named part-r-00000


        For simplicity, the examples so far have used files on the local filesystem. However, to scale out, we need to store the data in a distributed filesystem (typically HDFS).

        Mapper:
            Hadoop runs the job by dividing it into tasks, of which there are two types: map tasks and reduce tasks.

                - The tasks are scheduled using YARN and run on nodes in the cluster. If a task fails, it will be automatically rescheduled to run on a different node.
                - Hadoop divides the input to a MapReduce job into fixed-size pieces called input splits, or just splits.
                  Hadoop creates one map task for each split, which runs the user-defined map function for each record in the split.
                - Having many splits means the time taken to process each split is small compared to the time to process the whole input.
                  So if we are processing the splits in parallel, the processing is better load balanced when the splits are small, since a faster machine will be able to process proportionally more splits over the course of the job than a slower machine.
                  Even if the machines are identical, failed processes or other jobs running concurrently make load balancing desirable, and the quality of the load balancing increases as the splits become more fine grained.

                  On the other hand, if splits are too small, the overhead of managing the splits and map task creation begins to dominate the total job execution time.

                  For most jobs, a good split size tends to be the size of an HDFS block, which is 128 MB by default, although this can be changed for the cluster (for all newly created files) or specified when each file is created.

                  Hadoop does its best to run the map task on a node where the input data resides in HDFS, because it doesn’t use valuable cluster bandwidth. This is called the data locality optimization. Sometimes, however, all the nodes hosting the HDFS block replicas for a map task’s input split are running other map tasks, so the job scheduler will look for a free map slot on a node in the same rack as one of the blocks. Very occasionally even this is not possible, so an off-rack node is used, which results in an inter-rack network transfer. The three possibilities are illustrated in Figure 2-2.

                  (IMP) It should now be clear why the optimal split size is the same as the block size:
                  it is the largest size of input that can be guaranteed to be stored on a single node.
                  If the split spanned two blocks, it would be unlikely that any HDFS node stored both blocks, so some of the split would have to be transferred across the network to the node running the map task, which is clearly less efficient than running the whole map task using local data.

            Output of Mapper is stored on local disk. Why is this?
            Map output is intermediate output: it’s processed by reduce tasks to produce the final output, and once the job is complete, the map output can be thrown away. So, storing it in HDFS with replication would be overkill. If the node running the map task fails before the map output has been consumed by the reduce task, then Hadoop will automatically rerun the map task on another node to re-create the map output.

        Can you have multiple mappers in one job application?
        YES.
        See 'More Than One Mappers for Different Sets of Input Files' section in 'YARN(MapReduce2).docx'.

        Reducer:

            Reduce tasks don’t have the advantage of data locality; the input to a single reduce task is normally the output from all mappers.
            In the present example, we have a single reduce task that is fed by all of the map tasks.
            (IMP) Therefore, the sorted map outputs have to be transferred across the network to the node where the reduce task is running, where they are merged and then passed to the user-defined reduce function.

            The output of the reduce is normally stored in HDFS for reliability.
            Thus, writing the reduce output does consume network bandwidth, but only as much as a normal HDFS write pipeline consumes.

        Multiple Reducers:

            Output from mappers are sorted and grouped. This grouped records are Partitioned between multiple reducers.

            The number of reduce tasks is not governed by the size of the input, but instead is specified independently.

        See the diagram of map reduce flow on page 33.

        Combiner:

            Combiner’s code is exactly same as Reducer and it does exactly the same thing as Reducer.
            The only difference is that Combiner is on the same node where Mapper is.
            By doing reduction of Mapper’s output using Combiner reduces total output to be transferred through network to a Reducer. This is a key advantage of Combiner.



    Input split number vs blocks number

      https://stackoverflow.com/questions/30549261/split-size-vs-block-size-in-hadoop
      If you have 200 MB file and and default block size is 128MB.
      Two blocks will be created on different data nodes provided you have 2 or more data nodes.

      Input split is size of data to be assigned to one mapper. So, block is a physical split of data and input split is a logical split of data.
      Default input split size is same block size. So, if you don't specify number of mappers, hadoop will number of mappers same as number of blocks.
      If input split size=90 MB, then hadoop will create 3 mappers. In this case, there is a possibility that mapper is running on some node that needs to read block data from another node. This increases network latency. So, the best thing is to keep input split size same as block size.

*/

/*
For more detailed information about how to write MapReduce code, see Book's Chapter 6 (Developing a MapReduce Application)
It explains how can you switch easily between Standalone, Pseudo Distribution and Cluster modes.
It also explains how can you log and debug the malformed data by setting Job's status and Counter parameter and seeing them in Resource Manager's UI.
 */
public class WordCount {

    /*@Override
    public int run(String[] args) throws Exception {
        if(args.length < 2) {
            System.out.println("Please give input and output directories properly");
            return -1;
        }
        JobConf conf = new JobConf(WordCount.class);
        FileInputFormat.setInputPaths(conf, new Path(args[0]));
        FileOutputFormat.setOutputPath(conf, new Path(args[1]));

        conf.setMapperClass(WordMapper.class);
        conf.setReducerClass(WordReducer.class);

        conf.setOutputKeyClass(Text.class);
        conf.setOutputValueClass(IntWritable.class);

        jobClient.runJob(conf);
        return 0;
    }*/
    public static void main(String[] args) throws IOException,
            InterruptedException, ClassNotFoundException {

        Path inputFilePath = new Path("./SampleHadoopProject/input");
        Path outputFileDirPath = new Path("./SampleHadoopProject/output");

        // Create configuration
        // You can pass configuration params from command line also when you run a job that will supersede all configurations, if any.
        Configuration conf = new Configuration(true);
        //conf.set("confkeyname", "confvalue"); // you can set any key-value configuration
        //conf.addResource(new Path("./SampleHadoopProject/conf/core-site-override.xml")); // you can create an xml file that can have key-values. Normally, these kind of files are used to override files like core-site.xml etc.

        // Create job
        Job job = new Job(conf, "WordCount");
        job.setJarByClass(WordCountMapper.class); // Hadoop will find the job JAR automatically by searching for the JAR on the driver’s classpath that contains the class set in the setJarByClass() method (on JobConf or Job). Alternatively, if you want to set an explicit JAR file by its file path, you can use the setJar() method. (The JAR file path may be local or an HDFS file path.)

        //********* Mapper section *******************

        // Configure a Mapper
        job.setMapperClass(WordCountMapper.class);

        // InputFormat has information about RecordReader. RecordReader is responsible to convert InputSplit into key-value pair that can be fed to mapper class.
        // FileInputFormat is the base class for all implementations of InputFormat that use files as their data source.
        // TextInputFormat is one of the concrete class of FileInputFormat.
        // You can customize InputFormat class, if you want. Many of the parameters used by InputFormat are configurable, so in most cases, you don't have to customize InputFormat, but there can be a case like mentioned in 'How to force Not to create more than 1 split?'
        /*

        What is InputSplit?
            HDFS blocks are of size 128MB by default. It is possible that last record in a block is finishing in some other block.
            In this case, InputSplit will contain 2 blocks location information and size of a split will be little more than 128MB.

            Remember, blocks and splits are created for a file. If there are multiple input files, each file will be in different blocks and each file have different splits.
            You give a file path to InputFormat and InputFormat will decide how many splits need to be created based on your configuration that gives value of min split size and max split size. You can also override isSplitable() method to say that you don't want to split a file.


        What is RecordReader?

           RecordReader creates key-value pair from InputSplit and gives it to Mapper. Mapper accesses RecordReader using Context.

        InputFormat as FileInputFormat

            InputFormat is responsible to create InputSplit and RecordReader.

            You provide path(s) of file(s)/directory from which splits needs to be created based on split size related configuration parameter.

            FileInputFormat is responsible to create InputSplits.

            Who asks FileInputFormat to create InputSplits?
                JobSubmitter class
            Who calls JobSubmitter?
                Client
                So, Client actually creates InputSplits with the help of FileInputFormat and that information is stored in HDFS. Later on, Application Master will use this information to know how many tasks needs to be created.

             A path may represent a file, a directory, or, by using a glob, a collection of files and directories. A path representing a directory includes all the files in the directory as input to the job.
             To exclude certain files from the input, you can set a filter using the setInputPathFilter() method on FileInputFormat.
             Even if you don’t set a filter, FileInputFormat uses a default filter that excludes hidden files (those whose names begin with a dot or an underscore). If you set a filter by calling setInputPathFilter(), it acts in addition to the default filter. In other words, only nonhidden files that are accepted by your filter get through.

             Paths and filters can be set through configuration properties, too
             mapreduce.input.fileinputformat.inputdir, mapreduce.input.pathFilter.class

        FileInputFormat vs CombineFileInputFormat

             Where FileInputFormat creates a split per file, CombineFileInputFormat packs many files into each split so that each mapper has more to process.
             Crucially, CombineFileInputFormat takes node and rack locality into account when deciding which blocks to place in the same split, so it does not compromise the speed at which it can process the input in a typical MapReduce job.
             Of course, if possible, it is still a good idea to avoid the many small files case, because MapReduce works best when it can operate at the transfer rate of the disks in the cluster, and processing many small files increases the number of seeks that are needed to run a job. Also, storing large numbers of small files in HDFS is wasteful of the namenode’s memory.

        How to force Not to create more than 1 split?

            Some applications don’t want files to be split, as this allows a single mapper to process each input file in its entirety. For example, a simple way to check if all the records in a file are sorted is to go through the records in order, checking whether each record is not less than the preceding one. Implemented as a map task, this algorithm will work only if one map processes the whole file.2
            There are a couple of ways to ensure that an existing file is not split. The first (quick- and-dirty) way is to increase the minimum split size to be larger than the largest file in your system. Setting it to its maximum value, Long.MAX_VALUE, has this effect. The second is to subclass the concrete subclass of FileInputFormat that you want to use, to override the isSplitable() method3 to return false. For example, here’s a nonsplit‐ table TextInputFormat:

            import org.apache.hadoop.fs.Path;
            import org.apache.hadoop.mapreduce.JobContext;
            import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
            public class NonSplittableTextInputFormat extends TextInputFormat {
                @Override
                protected boolean isSplitable(JobContext context, Path file) {
                    return false;
                }
            }

            This should be avoided. There is no point of using Hadoop, if you are not going to have multiple mappers.
            You are not taking advantage of Hadoop’s parallel processing.

       */
        // TextInputFormat creates input key-value of types LongWritable-Text.
        // RecordReader of TextInputFormat converts a file into key-value pairs as start offset of a line - a line content and feeds it to a Mapper one key-value pair at a time.
        job.setInputFormatClass(TextInputFormat.class);

        FileInputFormat.addInputPath(job, inputFilePath);
        //FileInputFormat.setInputPaths(job, new Path(args[0])); // for AWS EMR

        //********** Partitioner *******************
        //job.setPartitionerClass(HashPartitioner.class); // HashPartitioner is a default Partitioner

        //********* Reducer section *******************
        // Reducer is not mandatory. If you don't have a reducer, then output of a Mapper becomes the final output of a job.
        // If you need to have a combiner, usually combiner's code is same as reducer, but it is not necessary to be like that.
        // but at least its input and output key-value types are same as reducer.
        job.setReducerClass(WordCountReducer.class);
        //job.setCombinerClass(WordCountReducer.class);

        // pg 217 of the book
        // One rule of thumb is to aim for reducers that each run for five minutes or so, and which produce at least one HDFS block’s worth of output.
        job.setNumReduceTasks(1); // by default, number of mappers is same as number of blocks in hdfs for a file.
        // You may have noticed that we didn’t set the number of map tasks.
        // The reason for this is that the number is equal to the number of splits that the input is turned into, which is driven by the size of the input and the file’s block size (if the file is in HDFS)
        // If you want, you can set a different number.

        //********* Job's Output Type ************************
        // Concrete class of InputFormat takes specific types key-value, e.g. TextInputFormat works with only LongWritable-Text type of key-value.
        // That's not the case with OutputFormat.
        // e.g. TextOutputFormat takes key-value of any types, so you need to specify OutputKeyClass and OutputValueClass
        // Its keys and values may be of any type, since TextOutputFormat turns them to strings by calling toString() on them
        // TextOutputFormat is a default output format
        // Each key-value pair is separated by a tab character, although that may be changed using the mapreduce.output.textoutputformat.separator proper‐ ty. The counterpart to TextOutputFormat for reading in this case is KeyValue TextInputFormat, since it breaks lines into key-value pairs based on a configurable separator.
        // LazyOutputFormat:
        // FileOutputFormat subclasses will create output (part-r-nnnnn) files, even if they are empty. Some applications prefer that empty files not be created, which is where LazyOutputFormat helps.
        LazyOutputFormat.setOutputFormatClass(job, TextOutputFormat.class);
        job.setOutputFormatClass(LazyOutputFormat.class);

        // Specify output's key and value types.
        // If there is a Reducer, this will be the output type of a Reducer.
        // If there is only a Mapper, this will be output type of a Mapper
        //FileOutputFormat.setOutputPath(job, new Path(args[1])); // for AWS EMR
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        // Output file path
        FileOutputFormat.setOutputPath(job, outputFileDirPath);

        // keep polling to know the completion status of the job
        job.waitForCompletion(true);

/*
        // Delete output if exists
        FileSystem hdfs = FileSystem.get(conf);
        if (hdfs.exists(outputFileDirPath))
            hdfs.delete(outputFileDirPath, true);

        // Execute job
        int code = job.waitForCompletion(true) ? 0 : 1;
        System.exit(code);
*/

    }
}
