package fastcampus.aop.part4.githubrepositoryproject.extensions

import android.content.res.Resources

//DP to PX
internal fun Float.fromDpToPx(): Int {
    return (this * Resources.getSystem().displayMetrics.density).toInt()
}