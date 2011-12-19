/*
 * LDIF
 *
 * Copyright 2011 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ldif.modules.silk.hadoop

import de.fuberlin.wiwiss.silk.hadoop.impl.EntityConfidence
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat._
import org.apache.hadoop.io.{NullWritable, SequenceFile, Text}
import ldif.hadoop.types.QuadWritable
import ldif.datasources.dump.QuadParser
import org.apache.hadoop.util.ReflectionUtils
import org.apache.hadoop.io.compress.{DefaultCodec, CompressionCodec}
import org.apache.hadoop.io.SequenceFile.CompressionType
import org.apache.hadoop.mapreduce.{RecordWriter, TaskAttemptContext}
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat
import ldif.util.Consts


class SameAsOutputFormat extends SequenceFileOutputFormat[Text, EntityConfidence] {

  override def getRecordWriter(job : TaskAttemptContext) : RecordWriter[Text, EntityConfidence] = {
    val config = job.getConfiguration
    val file = getDefaultWorkFile(job, "")
    val fs = file.getFileSystem(config)

    val codec = getCompressionCodec(job)
    val compressionType = getCompressionType(job)
    val writer = SequenceFile.createWriter(fs, config, file, classOf[NullWritable], classOf[QuadWritable], compressionType, codec)
    new LinkWriter(writer)
  }

  private class LinkWriter(writer : SequenceFile.Writer) extends RecordWriter[Text, EntityConfidence] {
    override def write(sourceUri : Text, entitySimilarity : EntityConfidence) {
      val line = "<" + sourceUri + "> <http://www.w3.org/2002/07/owl#sameAs> <" + entitySimilarity.targetUri + "> <"+Consts.SILK_OUT_GRAPH+"> .\n"
      val quadParser = new QuadParser()
      writer.append(NullWritable.get, new QuadWritable(quadParser.parseLine(line)))
    }

    override def close(context : TaskAttemptContext) {
      writer.close()
    }
  }

  // Get the CompressionType for the output SequenceFile
  def getCompressionType(context : TaskAttemptContext) : CompressionType = {
    if (getCompressOutput(context)) {
      val typeValue = context.getConfiguration.get("mapred.output.compression.type",CompressionType.RECORD.toString)
      CompressionType.valueOf(typeValue)
    }
    else CompressionType.NONE;
  }

  // Get the CompressionCodec for the output SequenceFile
  def getCompressionCodec(context : TaskAttemptContext) : CompressionCodec = {
    if (getCompressOutput(context)) {
      val codecClass = getOutputCompressorClass(context, classOf[DefaultCodec])
      ReflectionUtils.newInstance(codecClass, context.getConfiguration)
    }
    else null
  }
}