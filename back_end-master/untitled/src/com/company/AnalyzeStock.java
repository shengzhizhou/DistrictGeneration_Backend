import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import org.apache.commons.lang.WordUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.lib.output.*;
import org.apache.hadoop.util.StringUtils;

import java.net.URI;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.StringUtils;
import org.apache.log4j.Logger;


public class AnalyzeStock {

    public class MaxClosePriceMapper extends Mapper<LongWritable, Text, Text, FloatWritable> {
        private boolean caseSensitive = false;

        protected void setup(Mapper.Context context) throws IOException,InterruptedException {

            Configuration config = context.getConfiguration();
            this.caseSensitive = config.getBoolean("caseSensitive", false);
            URI[] localPaths = context.getCacheFiles();
            parseSkipFile(localPaths[0]);
        }
        private void parseSkipFile(URI patternsURI) {
            LOG.info("Added file to the distributed cache: " + patternsURI);
            try {
                BufferedReader fis = new BufferedReader(new FileReader(new File(patternsURI.getPath()).getName()));
                String pattern;
                while ((pattern = fis.readLine()) != null) {
                    patternsToSkip.add(pattern);
                }

                fis.close();
            } catch (IOException ioe) {
                System.err.println("Caught exception while parsing the cached file '"
                        + patternsURI + "' : " + StringUtils.stringifyException(ioe));
            }

        }

        public void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {

            String line = value.toString();
            String[] items = line.split(",");

            String stock = items[1];
            Float closePrice = Float.parseFloat(items[6]);

            context.write(new Text(stock), new FloatWritable(closePrice));

        }
    }



    public class MaxClosePriceReducer
            extends Reducer<Text, FloatWritable, Text, FloatWritable> {


        public void reduce(Text key, Iterable<FloatWritable> values, Context context)
                throws IOException, InterruptedException {

            float maxClosePrice = Float.MIN_VALUE;

            //Iterate all temperatures for a year and calculate maximum
            for (FloatWritable value : values) {
                maxClosePrice = Math.max(maxClosePrice, value.get());
            }

            //Write output
            context.write(key, new FloatWritable(maxClosePrice));
        }
    }
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
    Date date2=null;


    public static void main(String[] args)  throws Exception {
        Configuration conf = new Configuration();
        Job myjob = Job.getInstance(conf, "my word count test");
        myjob.setJarByClass(AnalyzeStock.class);
        myjob.setMapperClass(MaxClosePriceMapper.class);
        myjob.setCombinerClass(MaxClosePriceReducer.class);
        myjob.setReducerClass(MaxClosePriceReducer.class);
        myjob.setOutputKeyClass(Text.class);
        myjob.setOutputValueClass(LongWritable.class);
        // Uncomment to set the number of reduce tasks
        // myjob.setNumReduceTasks(2);
        FileInputFormat.addInputPath(myjob, new Path(args[0]));
        FileOutputFormat.setOutputPath(myjob,  new Path(args[1]));
        System.exit(myjob.waitForCompletion(true) ? 0 : 1);




    }
}