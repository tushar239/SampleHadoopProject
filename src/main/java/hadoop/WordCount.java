package hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;


/**
 * Created by chokst on 12/10/14.
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

        Path inputPath = new Path("./SampleHadoopProject/input");
        Path outputDir = new Path("./SampleHadoopProject/output");

        // Create configuration
        Configuration conf = new Configuration(true);

        // Create job
        Job job = new Job(conf, "WordCount");
        job.setJarByClass(WordCountMapper.class);

        // Setup MapReduce
        job.setMapperClass(WordCountMapper.class);
        job.setReducerClass(WordCountReducer.class);
        job.setNumReduceTasks(1);

        // Specify key / value
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        // Input
        FileInputFormat.addInputPath(job, inputPath);
        //FileInputFormat.setInputPaths(job, new Path(args[0])); // for AWS EMR
        job.setInputFormatClass(TextInputFormat.class);

        // Output
        FileOutputFormat.setOutputPath(job, outputDir);
        //FileOutputFormat.setOutputPath(job, new Path(args[1])); // for AWS EMR
        job.setOutputFormatClass(TextOutputFormat.class);

        //job.waitForCompletion(true); // for AWS EMR

        // Delete output if exists
        FileSystem hdfs = FileSystem.get(conf);
        if (hdfs.exists(outputDir))
            hdfs.delete(outputDir, true);

        // Execute job
        int code = job.waitForCompletion(true) ? 0 : 1;
        System.exit(code);

    }
}
