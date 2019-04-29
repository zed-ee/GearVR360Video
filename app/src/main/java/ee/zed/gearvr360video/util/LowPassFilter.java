package ee.zed.gearvr360video.util;

public class LowPassFilter {
    private static final float ALPHA = 0.20f;

    public static float[] lowPass( float[] input, float[] output ) {
        if ( output == null ) return input;

        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }
}
