#include <jni.h>
#include <cstdlib>
#include "lc3.h"

extern "C" {

// -------- Encoder --------
JNIEXPORT jlong JNICALL Java_com_vuzix_jnilc3_Lc3Codec_createEncoder(JNIEnv*, jclass,
                                            jint dtUs, jint srHz, jint srPcmHz) {
    unsigned sz = lc3_encoder_size(dtUs, srHz);
    void* mem = malloc(sz);
    lc3_encoder_t enc = lc3_setup_encoder(dtUs, srHz, srPcmHz, mem);
    return reinterpret_cast<jlong>(enc);
}

JNIEXPORT void JNICALL Java_com_vuzix_jnilc3_Lc3Codec_freeEncoder(JNIEnv*, jclass, jlong handle) {
    if (handle) {
        void* mem = reinterpret_cast<void*>(handle);
        free(mem);
    }
}

//JNIEXPORT void JNICALL Java_com_vuzix_jnilc3_Lc3Codec_disableLtpf(JNIEnv*, jclass, jlong handle) {
//    auto enc = reinterpret_cast<lc3_encoder_t>(handle);
//    lc3_encoder_disable_ltpf(enc);
//}

JNIEXPORT jint JNICALL Java_com_vuzix_jnilc3_Lc3Codec_encode(JNIEnv* env, jclass,
                                              jlong handle,
                                              jint fmt_,
                                              jbyteArray pcm_,
                                              jint stride,
                                              jbyteArray out_) {
    auto enc = reinterpret_cast<lc3_encoder_t>(handle);
    jbyte* pcm = env->GetByteArrayElements(pcm_, nullptr);
    jbyte* out = env->GetByteArrayElements(out_, nullptr);
    lc3_pcm_format fmt = static_cast<lc3_pcm_format>(fmt_);
    int nbytes = env->GetArrayLength(out_);

    int ret = lc3_encode(enc, fmt, pcm, stride, nbytes, out);

    env->ReleaseByteArrayElements(pcm_, pcm, JNI_ABORT);  // Read-only
    env->ReleaseByteArrayElements(out_, out, 0);          // Update the data
    return ret;
}

// -------- Decoder --------
JNIEXPORT jlong JNICALL Java_com_vuzix_jnilc3_Lc3Codec_createDecoder(JNIEnv*, jclass,
                                                    jint dtUs,
                                                    jint srHz,
                                                    jint srPcmHz) {
    unsigned sz = lc3_decoder_size(dtUs, srHz);
    void* mem = malloc(sz);
    lc3_decoder_t dec = lc3_setup_decoder(dtUs, srHz, srPcmHz, mem);
    return reinterpret_cast<jlong>(dec);
}

JNIEXPORT void JNICALL Java_com_vuzix_jnilc3_Lc3Codec_freeDecoder(JNIEnv*, jclass, jlong handle) {
    if (handle) {
        void* mem = reinterpret_cast<void*>(handle);
        free(mem);
    }
}

JNIEXPORT jint JNICALL Java_com_vuzix_jnilc3_Lc3Codec_decode(JNIEnv* env, jclass, jlong handle,
                                                            jbyteArray in_,
                                                            jint fmt_,
                                                            jbyteArray pcm_,
                                                            jint stride) {
    auto dec = reinterpret_cast<lc3_decoder_t>(handle);
    jbyte* in = in_ ? env->GetByteArrayElements(in_, nullptr) : nullptr;
    jbyte* pcm = env->GetByteArrayElements(pcm_, nullptr);
    lc3_pcm_format fmt = static_cast<lc3_pcm_format>(fmt_);
    int nbytes = env->GetArrayLength(in_);

    int ret = lc3_decode(dec, in, nbytes, fmt, pcm, stride);

    env->ReleaseByteArrayElements(in_, in, JNI_ABORT);    // Read-only
    env->ReleaseByteArrayElements(pcm_, pcm, 0);          // Update the output data
    return ret;
}

} // extern "C"
