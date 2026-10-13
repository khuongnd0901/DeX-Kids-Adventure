#include <jni.h>
#include <malloc.h>

/* Android public allocator API: only pages already free, never live objects.
 * Java gates M_PURGE_ALL to API34+; M_PURGE is available since API28.
 */
JNIEXPORT jboolean JNICALL
Java_com_khuongnd_dexkids_NativeHeapTrim_nativePurge(JNIEnv* env, jclass cls, jboolean deep) {
    (void)env;
    (void)cls;
    return mallopt(deep ? M_PURGE_ALL : M_PURGE, 0) ? JNI_TRUE : JNI_FALSE;
}
