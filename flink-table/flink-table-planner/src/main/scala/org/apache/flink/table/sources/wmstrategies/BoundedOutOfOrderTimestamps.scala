/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.table.sources.wmstrategies

import java.util

import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.table.descriptors.Rowtime

/**
  * A watermark strategy for rowtime attributes which are out-of-order by a bounded time interval.
  *
  * Emits watermarks which are the maximum observed timestamp minus the specified delay.
  *
  * @param delay The delay by which watermarks are behind the maximum observed timestamp.
  */
final class BoundedOutOfOrderTimestamps(val delay: Long) extends PeriodicWatermarkAssigner {

  var maxTimestamp: Long = Long.MinValue + delay

  override def nextTimestamp(timestamp: Long): Unit = {
    if (timestamp > maxTimestamp) {
      maxTimestamp = timestamp
    }
  }

  override def getWatermark: Watermark = new Watermark(maxTimestamp - delay)

  override def equals(other: Any): Boolean = other match {
    case that: BoundedOutOfOrderTimestamps =>
      delay == that.delay
    case _ => false
  }

  override def hashCode(): Int = {
    delay.hashCode()
  }

  override def toProperties: util.Map[String, String] = {
    val javaMap = new util.HashMap[String, String]()
    javaMap.put(
      Rowtime.ROWTIME_WATERMARKS_TYPE,
      Rowtime.ROWTIME_WATERMARKS_TYPE_VALUE_PERIODIC_BOUNDED)
    javaMap.put(Rowtime.ROWTIME_WATERMARKS_DELAY, delay.toString)
    javaMap
  }
}
