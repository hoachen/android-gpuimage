package jp.co.cyberagent.android.gpuimage.filter;

import android.graphics.PointF;
import android.opengl.GLES20;

public class GPUImageMagnifierFilter extends GPUImageFilter {

    public static final String MAGNIFITER_FRAGMENT_SHADER = "" +
            "varying highp vec2 textureCoordinate;\n" +
            "\n" +
            "uniform sampler2D inputImageTexture;\n" +
            "uniform highp vec2 in_circle_pos;\n" +
            "uniform highp float in_zoom_times;\n" +
            "uniform highp float in_circle_radius;\n" +
            "uniform highp float image_width;\n" +
            "uniform highp float image_height;\n" +
            "\n" +
            "vec2 transForTexPosition(vec2 pos)\n" +
            "{\n"+
            "    return vec2(float(pos.x/1.0f), float(pos.y/1.0f));\n" +
            "}\n" +
            "\n" +
            "float getDistance(vec2 pos_src, vec2 pos_dist)\n" +
            "{\n" +
            "    float ratio = image_width * 1.0 / image_height;\n" +
            "    float quadratic_sum = 1.0;\n" +
//            "pow((pos_src.x-pos_dist.x),2.)+pow((pos_src.y-pos_dist.y) * 1.0/ratio,2.);\n" +
            "    if (ratio < 1.0) { \n" +
            "        quadratic_sum = pow((pos_src.x-pos_dist.x),2.)+pow((pos_src.y-pos_dist.y) * 1.0/ratio,2.);\n" +
            "    } else {\n" +
            "        quadratic_sum = pow((pos_src.x-pos_dist.x),2.)+pow((pos_src.y-pos_dist.y) * ratio,2.);\n" +
            "    }\n" +
            "    return sqrt(quadratic_sum);\n" +
            "}\n" +
            "\n" +
            "vec2 getZoomPosition()\n" +
            "{\n" +
            "    float zoom_x = float(textureCoordinate.x-in_circle_pos.x)/in_zoom_times;\n" +
            "    float zoom_y = float(textureCoordinate.y-in_circle_pos.y)/in_zoom_times;\n" +
            "    return vec2(float(in_circle_pos.x+zoom_x),float(in_circle_pos.y+zoom_y));\n" +
            "}"  +
            "\n" +
            "vec4 getColor()\n" +
            "{\n" +
            "    vec2 pos = getZoomPosition();\n" +
//            "    float _x = floor(pos.x);\n" +
//            "    float _y = floor(pos.y);\n" +
//            "    float u = pos.x -_x;\n" +
//            "    float v = pos.y-_y;\n" +
//            "    vec4 data_00 = texture2D(inputImageTexture, vec2(_x,_y));\n" +
//            "    vec4 data_01 = texture2D(inputImageTexture, vec2(_x, _y+1));\n" +
//            "    vec4 data_10 = texture2D(inputImageTexture, vec2(_x + 1., _y));\n" +
//            "    vec4 data_11 = texture2D(inputImageTexture, vec2(_x+1.,_y+1.));\n" +
//            "    return (1.-u)*(1.-v)*data_00 +(1.-u)*v*data_01+u*(1.-v)*data_10 + u*v*data_11;\n" +
            "    return texture2D(inputImageTexture, pos);\n" +
            "}\n"+
            "\n" +
            "void main()\n" +
            "{\n" +
            "    vec2 frag_pos = vec2(textureCoordinate.x, textureCoordinate.y);\n" +
            "    float distance = getDistance(in_circle_pos, frag_pos);         \n" +
            "    if(distance > (in_circle_radius + 0.01)) { \n" +
            "        gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n"+
            "    } else { \n"+
            "        if (distance >= in_circle_radius) {\n" +
            "            gl_FragColor = vec4(0.0, 0.0, 0.0, 0.6);\n" +
            "        } else {\n" +
            "            gl_FragColor = getColor();\n" +
            "        }\n" +
            "    }\n" +
            "}\n";

    private PointF centerPos;
    private float radius;
    private float zoom;
    private int circleCenterLocation;
    private int radiusLocation;
    private int zoomLocation;
    private float imageWidth;
    private float imageHeight;
    private int imageWidthLocation;
    private int imageHeightLocation;

    public GPUImageMagnifierFilter() {
        this(new PointF(0.45f, 0.35f), 0.35f, 2.0f);
    }

    public GPUImageMagnifierFilter(PointF centerPos, float radius, float zoom) {
        super(NO_FILTER_VERTEX_SHADER, MAGNIFITER_FRAGMENT_SHADER);
        this.centerPos = centerPos;
        this.radius = radius;
        this.zoom = zoom;
    }

    @Override
    public void onInit() {
        super.onInit();
        circleCenterLocation = GLES20.glGetUniformLocation(getProgram(), "in_circle_pos");
        radiusLocation = GLES20.glGetUniformLocation(getProgram(), "in_circle_radius");
        zoomLocation = GLES20.glGetUniformLocation(getProgram(), "in_zoom_times");
        imageWidthLocation = GLES20.glGetUniformLocation(getProgram(), "image_width");
        imageHeightLocation = GLES20.glGetUniformLocation(getProgram(), "image_height");
    }

    @Override
    public void onInitialized() {
        super.onInitialized();
        setCircleCenter(centerPos);
        setRadius(radius);
        setZoom(zoom);
        setImageSize(640, 640);
    }

    @Override
    public void onOutputSizeChanged(int width, int height) {
        super.onOutputSizeChanged(width, height);
        setImageSize(width, height);
    }

    public void setCircleCenter(final PointF center) {
        this.centerPos = center;
        setPoint(circleCenterLocation, centerPos);
    }

    public void setRadius(final float radius) {
        this.radius = radius;
        setFloat(radiusLocation, radius);
    }

    public void setZoom(final float zoom) {
        this.zoom = zoom;
        setFloat(zoomLocation, zoom);
    }

    public void setImageSize(final float w, float h) {
        this.imageWidth  = w;
        this.imageHeight  = h;
        setFloat(imageWidthLocation, imageWidth);
        setFloat(imageHeightLocation, imageHeight);
    }
}
