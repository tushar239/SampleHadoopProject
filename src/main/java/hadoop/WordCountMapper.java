package hadoop;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.StringTokenizer;

/**
 * How to log and debug the errors or malformed input data?
 * you can set job's status and also create a counter for malformed data.
 * You can easily see this information using Resource Manager's UI.
 * See pg 168 of the book.
 */

/*

Mapper syntax

public class Mapper<KEYIN, VALUEIN, KEYOUT, VALUEOUT> {

   protected void map(KEYIN key, VALUEIN value, Context context) throws IOException, InterruptedException {

     context.write((KEYOUT) key, (VALUEOUT) value);

   }
}

HashPartitioner syntax

public class HashPartitioner<K, V> extends Partitioner<K, V> {

    public int getPartition(K key, V value, int numReduceTasks) {

        return (key.hashCode() & Integer.MAX_VALUE) % numReduceTasks;

    }

}

The default partitioner is HashPartitioner, which hashes a record’s key to determine which partition the record belongs in.
Each partition is processed by a reduce task, so the number of partitions is equal to the number of reduce tasks for the job.
 */

public class WordCountMapper
        extends Mapper<LongWritable, Text, // data types of input key-value
                Text, IntWritable> { // data types of output key-value

    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();

    // Every Mapper class overrides map method
    @Override
    public void map(LongWritable key, Text value, // data types of input key-value
                    Context context)
            throws IOException, InterruptedException {
        String line = value.toString();
        StringTokenizer itr = new StringTokenizer(line, ",");

        while (itr.hasMoreTokens()) {
            //just added the below line to convert everything to lower case
            word.set(itr.nextToken().toLowerCase());
            // the following check is that the word starts with an alphabet.
            if(Character.isAlphabetic((word.toString().charAt(0)))){
                context.write(word, one);
            }
        }
    }

}