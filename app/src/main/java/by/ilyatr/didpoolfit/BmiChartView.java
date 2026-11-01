package by.ilyatr.didpoolfit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

public class BmiChartView extends View {

    static final float MAX_BMI = 40;

    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    RectF sector = new RectF(1, 1, 99, 99);
    LinearGradient gradient = new LinearGradient(14, 1, 99, 80,
            Color.parseColor("#D77DE5"), Color.parseColor("#C451F5"), Shader.TileMode.CLAMP);
    float angle = 0;
    String value;

    public BmiChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setTypeface(ResourcesCompat.getFont(context, R.font.poppins_bold));
        paint.setTextSize(12);
        paint.setTextAlign(Paint.Align.CENTER);
        value = getResources().getString(R.string.bmi_no_data);
    }

    public void setBmi(float bmi) {
        if (Float.isNaN(bmi) || Float.isInfinite(bmi) || bmi <= 0) {
            angle = 0;
            value = getResources().getString(R.string.bmi_no_data);
        } else {
            angle = Math.min(bmi / MAX_BMI, 1) * 360;
            value = getResources().getString(R.string.bmi_value, bmi);
        }
        setContentDescription(getResources().getString(R.string.bmi_chart_value, value));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float size = Math.min(getWidth(), getHeight());
        canvas.save();
        canvas.translate((getWidth() - size) / 2, (getHeight() - size) / 2);
        canvas.scale(size / 100, size / 100);

        paint.setShader(null);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(50, 50, 42, paint);

        if (angle > 0) {
            paint.setShader(gradient);
            canvas.drawArc(sector, -100, angle, true, paint);
            paint.setShader(null);
        }

        // Число следует за серединой цветного сектора.
        double middle = Math.toRadians(-100 + angle / 2);
        float x = angle > 0 ? 50 + 27 * (float) Math.cos(middle) : 50;
        float y = angle > 0 ? 50 + 27 * (float) Math.sin(middle) : 50;
        paint.setColor(angle > 0 ? Color.WHITE : Color.parseColor("#C451F5"));
        canvas.drawText(value, x, y - (paint.ascent() + paint.descent()) / 2, paint);
        canvas.restore();
    }
}
