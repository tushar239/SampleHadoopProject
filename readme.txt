// Learning MapReduce by Nitesh J.
// Word Count Mapper.
import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
// MAPPER-
// InputSplit is an input to all Mappers. Default sie of InputSplit is same as size of HDFS block.
// While running MapReduce, you need to specify the type of InputFormat. Default type is TextInputFormat.
// InputFormat reads one line at a time by default from InputSplit and creates key-value pair from it and feeds that key-value to a Mapper.
// TextInputFormat considers key as byte offset of a line and value as a line itself.
// So, data type of key will be LongWritable and data type of value will be Text.
// That is why in this Mapper class input key-value's datatypes are defined as LongWritable and Text.

// Output of each Mapper is also key-value.
// In this case, if InputSplit has
//          hi how are you
//          hi how are you
// then input to map method will be
//          (0, hi how are you)  --- \n is also considered
//          (16, hi how are you)
// So, map method will be called twice, once for each key-value
// output of map method it will be like
//          (hi,1), (how,1), (are,1), (you,1)
//          (hi,1), (how,1), (are,1), (you,1)
// It is also key-value of type Text and IntWritable
// These key-value pairs are stored in local disk (not in HDFS) because it is just an intermident result.

// If you don't write your own Mapper, Hadoop will use implicit Mapper called IdentityMapper, which actually outputs the same key-value pairs as inputs.

// COMBINER -
// will do shuffling and sorting of output key-value pairs of Mapper. Number of Combiners are same as number of Mappers.
// and they are on the same DataNode as Mapper.
// Normally, shuffling and sorting is a task of Reducer, but if output of Mappers is very high, then it takes time to send these key-values to Reducer.
// It is better that one round of shuffling and sorting happens before the output of Mappers written to local disk.
// Shuffling Output
// (hi, [1,1]), (how, [1,1]), (are, [1,1]), (you, [1,1])
// Sorting Output (sorted by key)
// (are, [1,1]), (hi, [1,1]),  (how, [1,1]),(you, [1,1])
// Now, only 4 key-value pairs will written to local disk, which will be input to Reducer.
// Likewise, output from each mapper's combiner will be written to local disc.
// If ther is no Combiner, then output of Mapper will be written to local disc.

// PARTITIONER -
// If there are more than 1 Reducers, then Partitioner decides which key-value pair goes to which Reducer.
// Default Partitioner is HashPartitioner that routes key-value to a Reducer as per key's hashcode, so all same keys goes to same Reducer.
// You can customize the Partitioner.

// REDUCER -
// Finally reducer will do Shuffling and Sorting and call reduce() method.
// That will also create key-value pairs as output.
// Reducer's output is final output, so it will be written to HDFS DataNodes with all Replication strategy.
// Reducer job takes time because of network latency to send input key-values to it. Hadoop cannot afford to loose its output by storing it on local disk like Mapper's output.
// Once Reducer's task is successfully finished, JobTracker will remove Mapper's output from local disk.

// It is TaskTracker's responsibility to run Mappers and keep sending its updates to JobTracker.
// JobTracker has metadata of all TaskTrackers and running jobs on TaskTrackers.
// Both JobTracker and TaskTrackers are Java Processes.
// JobTracker resides on Master Node where as TaskTracker resides on each DataNode.
// If JobTracker node (Master node) goes down, then metadata of all jobs are lost. So, Master node should have good hardware just like NameNode in HDFS.



