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