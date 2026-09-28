package com.example.irssa

import java.io.DataInputStream
import java.io.InputStream
import kotlin.math.sqrt

data class Landmark(val x: Float,val y: Float,val z: Float)
data class Prediction(val label: String,val agreement: Float,val distance: Float,val accepted: Boolean)
object LandmarkFeatures {
  fun normalize(points: List<Landmark>): FloatArray? {
    if(points.size!=21 || points.any { !it.x.isFinite() || !it.y.isFinite() || !it.z.isFinite() }) return null
    val wrist=points[0];val sign=if(points[5].x<points[17].x) -1f else 1f
    var scale=0f;val result=FloatArray(63)
    points.forEachIndexed { i,p ->
      val x=(p.x-wrist.x)*sign;val y=p.y-wrist.y;val z=p.z-wrist.z
      result[i*3]=x;result[i*3+1]=y;result[i*3+2]=z;scale=maxOf(scale,sqrt(x*x+y*y+z*z))
    }
    if(scale<0.0001f) return null
    for(i in result.indices) result[i]/=scale
    return result
  }
}
/** Distance-weighted 5-NN with explicit distance and ambiguity rejection. */
class LandmarkClassifier(input: InputStream) {
  private val labels: List<String>;private val features: Array<FloatArray>;private val targets: IntArray
  init { DataInputStream(input.buffered()).use { s ->
    require(s.readInt()==0x49525353);val count=s.readInt();require(count in 1..20000);require(s.readInt()==63)
    val classes=s.readInt();require(classes in 1..64);labels=List(classes) { s.readUTF() };targets=IntArray(count)
    features=Array(count) { i -> targets[i]=s.readInt();require(targets[i] in labels.indices);FloatArray(63) { s.readFloat() } }
  } }
  fun predict(points: List<Landmark>): Prediction? {
    val query=LandmarkFeatures.normalize(points) ?: return null
    val distances=FloatArray(5) { Float.POSITIVE_INFINITY };val neighbors=IntArray(5)
    for(i in features.indices) {
      var d=0f;for(j in 0 until 63) { val delta=query[j]-features[i][j];d+=delta*delta }
      if(d<distances[4]) { var k=4
        while(k>0 && d<distances[k-1]) { distances[k]=distances[k-1];neighbors[k]=neighbors[k-1];k-- }
        distances[k]=d;neighbors[k]=targets[i]
      }
    }
    val votes=FloatArray(labels.size)
    for(k in 0..4) if(distances[k].isFinite()) votes[neighbors[k]]+=1f/(sqrt(distances[k])+0.00001f)
    val winner=votes.indices.maxByOrNull { votes[it] } ?: return null
    val agreement=votes[winner]/votes.sum();val rms=sqrt(distances[0]/63f)
    // Agreement is not calibrated probability and is never displayed as accuracy.
    // The gate is deliberately strict: the training hands are one webcam session, so a
    // looser gate cross-confirms unseen hands. Tuned on held-out data: no false
    // confirmations, 71% of correct handshapes still accepted.
    return Prediction(labels[winner],agreement,rms,agreement>=0.95f && rms<=0.07f)
  }
}
data class HoldResult(val progress: Float=0f,val confirmed: Boolean=false)
/** Requires continuous evidence, then hand removal before accepting another sign. */
class StableSignGate(private val holdMillis: Long=1200,private val releaseMillis: Long=550) {
  private var label: String?=null;private var start=0L;private var previous=0L
  private var latched=false;private var missingSince: Long?=null
  fun reset() { label=null;start=0;previous=0;latched=false;missingSince=null }
  fun update(candidate: String?,time: Long,handPresent: Boolean=candidate!=null): HoldResult {
    if(candidate==null) {
      if(handPresent) missingSince=null else if(missingSince==null) missingSince=time
      if(!handPresent && time-(missingSince ?: time)>=releaseMillis) latched=false
      label=null;start=time;previous=time;return HoldResult()
    }
    missingSince=null;if(latched) return HoldResult(1f,false)
    if(candidate!=label || time-previous>500 || time<previous) { label=candidate;start=time }
    previous=time;val progress=((time-start).toFloat()/holdMillis).coerceIn(0f,1f)
    if(progress>=1f) { latched=true;return HoldResult(1f,true) }
    return HoldResult(progress,false)
  }
}
