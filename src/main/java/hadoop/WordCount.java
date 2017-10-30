package hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;


/*
 From "Hadoop The Definitive Guide 4th Edition" Book
 pg 23-27

 Rather than using built-in Java types, Hadoop provides its own set of basic types that are op‐ timized for network serialization. These are found in the org.apache.hadoop.io pack‐ age. Here we use LongWritable, which corresponds to a Java Long, Text (like Java String), and IntWritable (like Java Integer).

 The input types of the reduce function must match the output types of the map function: Text and IntWritable.

 static addInputPath() method on FileInputFormat, and it can be a single file, a directory (in which case, the input forms all the files in that direc‐ tory), or a file pattern. As the name suggests, addInputPath() can be called more than once to use input from multiple paths.

 The setOutputKeyClass() and setOutputValueClass() methods control the output types for the reduce function, and must match what the Reduce class produces. The map output types default to the same types, so they do not need to be set if the mapper produces the same types as the reducer (as it does in our case). However, if they are different, the map output types must be set using the setMapOutputKeyClass() and setMapOutputValueClass() methods.

 The waitForCompletion() method on Job submits the job and waits for it to finish. The single argument to the method is a flag indicating whether verbose output is generated. When true, the job writes information about its progress to the console.
 The return value of the waitForCompletion() method is a Boolean indicating success (true) or failure (false), which we translate into the program’s exit code of 0 or 1.

 Commands to run this job
    hadoop jar ./target/SampleHadoopProject-1.0-SNAPSHOT.jar hadoop.WordCount ./input ./output

 You can run the this main class from IntelliJ also.

 Somehow, I could not make hadoop work properly in my local mac. So, I used 'Cloudera Hadoop VM' on 'VMWare Fusion for Mac'.
 Cloudera Hadoop VM has all installed and configured for Hadoop and and its Ecosystem libraries and tools.
 You can read 'Running WordCount job on Cloudera VM.txt' under '/Books/HadoopEcosystem/hadoop distributions/Clouder Hadoop on VMWare'
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

        job.waitForCompletion(true); // for AWS EMR

/*
        // Delete output if exists
        FileSystem hdfs = FileSystem.get(conf);
        if (hdfs.exists(outputDir))
            hdfs.delete(outputDir, true);

        // Execute job
        int code = job.waitForCompletion(true) ? 0 : 1;
        System.exit(code);
*/

    }
}
