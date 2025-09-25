/*
 *
 *  Copyright 2022 Google LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at:
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.vuzix.jnilc3;

/**
 * Low Complexity Communication Codec (LC3)
 *
 * <p>This implementation conforms to:
 *
 * <ul>
 *   <li>Low Complexity Communication Codec (LC3), Bluetooth Specification v1.0</li>
 *   <li>ETSI TS 103 634 v1.4.1 Digital Enhanced Cordless Telecommunications (DECT)
 *       Low Complexity Communication Codec plus (LC3plus)</li>
 * </ul>
 *
 * <p>LC3 and LC3 Plus are audio codecs designed for low-latency audio transport.
 *
 * <p>Unlike most other codecs, the LC3 codec is focused on audio streaming
 * in constrained (on packet sizes and interval) transport layers. In this way,
 * LC3 does not handle:
 *
 * <ul>
 *   <li>VBR (Variable Bitrate), based on input signal complexity</li>
 *   <li>ABR (Adaptive Bitrate)</li>
 * </ul>
 *
 * <p>It does not rely on any bit reservoir; a frame will be strictly encoded
 * within the byte budget given by the user (or transport layer).
 *
 * <p>However, the bitrate (bytes budget for encoding a frame) can be
 * freely changed at any time. It does not rely on signal complexity, but it
 * can follow a temporary bandwidth increase or reduction.
 *
 * <p>Unlike classic codecs, the LC3 codec does not run on a fixed number
 * of samples as input. It operates only on fixed frame durations, for
 * any supported sample rate (8 to 48 KHz). Two frame durations are
 * available: 7.5 ms and 10 ms.
 *
 * <h3>LC3 Plus features</h3>
 *
 * <p>In addition to LC3, the following features of LC3 Plus are provided:
 *
 * <ul>
 *   <li>Frame durations of 2.5 and 5 ms</li>
 *   <li>High-Resolution mode, 48 KHz and 96 KHz sampling rates</li>
 * </ul>
 *
 * <p>The distinction between LC3 and LC3 Plus is made according to:
 *
 * <table border="1" cellpadding="4" cellspacing="0">
 *   <tr><th>Frame Duration</th><th>2.5 ms</th><th>5 ms</th><th>7.5 ms</th><th>10 ms</th></tr>
 *   <tr><td>LC3</td><td></td><td></td><td>X</td><td>X</td></tr>
 *   <tr><td>LC3 Plus</td><td>X</td><td>X</td><td></td><td>X</td></tr>
 * </table>
 *
 * <p>The 10 ms frame duration is available in both LC3 and LC3 Plus.
 * In this mode, the produced bitstream can be referenced as either LC3 or LC3 Plus.
 *
 * <p>The LC3 Plus high-resolution mode should be preferred at high bitrates
 * and larger audio bandwidth. In this mode, the audio bandwidth is always
 * up to the Nyquist frequency, compared to LC3 at 48 KHz, which limits
 * the bandwidth to 20 KHz.
 *
 * <h3>Bitrate</h3>
 *
 * <p>The proposed implementation accepts any frame sizes between 20 and 400 bytes
 * in non-high-resolution mode. Note that the LC3 Plus standard defines
 * smaller sizes for frame durations shorter than 10 ms and/or sampling rates
 * less than 48 kHz.
 *
 * <p>In High-Resolution mode, the frame sizes (and bitrates) are restricted
 * as follows:
 *
 * <table border="1" cellpadding="4" cellspacing="0">
 *   <tr><th>HR Configuration</th><th>Frame Sizes</th><th>Bitrate (kbps)</th></tr>
 *   <tr><td>10 ms - 48 KHz</td><td>156 to 625</td><td>124.8 – 500</td></tr>
 *   <tr><td>10 ms - 96 KHz</td><td>187 to 625</td><td>149.6 – 500</td></tr>
 *   <tr><td>5 ms - 48 KHz</td><td>93 to 375</td><td>148.8 – 600</td></tr>
 *   <tr><td>5 ms - 96 KHz</td><td>109 to 375</td><td>174.4 – 600</td></tr>
 *   <tr><td>2.5 ms - 48 KHz</td><td>54 to 210</td><td>172.8 – 672</td></tr>
 *   <tr><td>2.5 ms - 96 KHz</td><td>62 to 210</td><td>198.4 – 672</td></tr>
 * </table>
 *
 * <h3>About 44.1 KHz sample rate</h3>
 *
 * <p>The Bluetooth specification and ETSI TS 103 634 standard reference
 * the 44.1 KHz sample rate, although there is no support in the core algorithm
 * of the codec.
 *
 * <p>We can summarize the 44.1 KHz support by: "You can put any sample rate around
 * the defined base sample rates." Please note:
 *
 * <ol>
 *   <li>The frame size will not be 2.5 ms, 5 ms, 7.5 ms, or 10 ms, but is scaled
 *       by <code>supported_sample_rate / input_sample_rate</code>.</li>
 *   <li>The bandwidth will be hard-limited (to 20 KHz) if you select 48 KHz.
 *       The encoded bandwidth will also be affected by the above inverse
 *       factor of 20 KHz.</li>
 * </ol>
 *
 * <p>Applied to 44.1 KHz, we get:
 *
 * <ul>
 *   <li>About 8.16 ms frame duration, instead of 7.5 ms</li>
 *   <li>About 10.88 ms frame duration, instead of 10 ms</li>
 *   <li>Bandwidth becomes limited to 18.375 KHz</li>
 * </ul>
 *
 * <h3>How to encode / decode</h3>
 *
 * <p>An encoder/decoder context needs to be set up. This context keeps state
 * on the current stream to process, and samples that overlap across
 * frames.
 *
 * <p>You have two ways to set up the encoder/decoder:
 *
 * <ul>
 *   <li><b>Static memory allocation:</b>
 *       The types <cod*
 */
public class Lc3Codec {

    // Used to load the 'jnilc3' library on application startup.
    static {
        System.loadLibrary("jnilc3");
    }


    /**
     * PCM Sample Format
     */
    public enum lc3_pcm_format {
        /**Signed 16 bits, in 16 bits words (int16_t) */
        LC3_PCM_FORMAT_S16,
        /** Signed 24 bits, using low three bytes of 32 bits words (int32_t).
         The high byte sign extends (bits 31..24 set to b23). */
        LC3_PCM_FORMAT_S24,
        /** Signed 24 bits packed in 3 bytes little endian */
        LC3_PCM_FORMAT_S24_3LE,
        /** Floating point 32 bits (float type), in range -1 to 1 */
        LC3_PCM_FORMAT_FLOAT,
    };


    // Encoder
    private static native long createEncoder(int dtUs, int srHz, int srPcmHz);
    private static native void freeEncoder(long handle);
    //private static native void disableLtpf(long handle);
    private static native int encode(long handle, int pcmFormat,
                                    byte[] pcm, int stride, byte[] out);

    /**
     * Class to manage encoding from PCM to LC3
     */
    public static class Encoder {
        private long memHandle;


        /**
         * Setup encoder
         * @param dtUs           Frame duration in us, 2500, 5000, 7500 or 10000
         * @param srHz           Sample rate in Hz, 8000, 16000, 24000, 32000, 48000 or 96000
         * @param  srPcmHz       Input sample rate, downsampling option of input, or 0
         * @throws               IllegalArgumentException
         *
         * The `sr_pcm_hz` parameter is a downsampling option of PCM input,
         * the value `0` fallback to the sample rate of the encoded stream `sr_hz`.
         * When used, `sr_pcm_hz` is intended to be higher or equal to the encoder
         * sample rate `sr_hz`. The size of the context needed, given by
         * `lc3_hr_encoder_size()` will be set accordingly to `sr_pcm_hz`.
         */
        public Encoder(int dtUs, int srHz, int srPcmHz) throws IllegalArgumentException {
            memHandle =  createEncoder(dtUs, srHz, srPcmHz);
            if (memHandle == 0) {
                throw new IllegalArgumentException("Native call rejected the parameter combination.");
            }
        }

        /**
         * Free memory and clean up. Calling this is mandatory if you have created an Encoder.
         */
        public void close() {
            if (memHandle != 0) {
                freeEncoder(memHandle);
                memHandle = 0;
            }
        }

        /**
         * Encode a frame
         * @param pcmFormat PCM input format of type {@link com.vuzix.jnilc3.Lc3Codec.lc3_pcm_format}
         * @param pcm Input PCM samples
         * @param stride  Count between two consecutives in pcm
         * @param out     Output buffer of exactly one frame size
         * @return          0: On success  -1: Wrong parameters
         */
        public int encodePcm(lc3_pcm_format pcmFormat,
                             byte[] pcm, int stride, byte[] out) {
            if(memHandle != 0) {
                return encode(memHandle, pcmFormat.ordinal(), pcm, stride, out);
            }
            return -1;
        }
    }

    // Decoder
    private static native long createDecoder(int dtUs, int srHz, int srPcmHz);
    private static native void freeDecoder(long handle);
    private static native int decode(long handle,
                                    byte[] in,
                                    int pcmFormat,
                                    byte[] pcm, int stride);
    /**
     * Class to manage decoding from LC3 to PCM
     */
    public static class Decoder {
        private long memHandle;
        /**
         * Setup decoder
         * @param dtUs           Frame duration in us, 2500, 5000, 7500 or 10000
         * @param srHz           Sample rate in Hz, 8000, 16000, 24000, 32000, 48000 or 96000
         * @param srPcmHz        Output sample rate, upsampling option of output (or 0)
         * @throws               IllegalArgumentException
         *
         * The `sr_pcm_hz` parameter is an upsampling option of PCM output,
         * the value `0` fallback to the sample rate of the decoded stream `sr_hz`.
         * When used, `sr_pcm_hz` is intended to be higher or equal to the decoder
         * sample rate `sr_hz`.
         */
        public Decoder(int dtUs, int srHz, int srPcmHz) throws IllegalArgumentException {
            memHandle =  createDecoder(dtUs, srHz, srPcmHz);
            if (memHandle == 0) {
                throw new IllegalArgumentException("Native call rejected the parameter combination.");
            }
        }

        /**
         * Free memory and clean up. Calling this is mandatory if you have created a Decoder.
         */
        public void close() {
            if (memHandle != 0) {
                freeDecoder(memHandle);
                memHandle = 0;
            }
        }

        /**
         * Decode a frame
         * @param in        Input byte stream of LC3
         * @param pcmFormat PCM input format of type {@link com.vuzix.jnilc3.Lc3Codec.lc3_pcm_format}
         * @param pcm       Output PCM samples (must be allocated big enough to hold results)
         * @param stride    count between two consecutives in PCM samples
         * @return          0: On success  1: PLC operated  -1: Wrong parameters
         */
        public int decodeLc3(byte[] in,
                             lc3_pcm_format pcmFormat,
                             byte[] pcm, int stride) {
            if(memHandle != 0) {
                return decode(memHandle, in, pcmFormat.ordinal(), pcm, stride);
            }
            return -1;
        }
    }
}