package com.baymax.healthcare

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.ContextCompat

class BaymaxEye @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val eyePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var isListening = false
    private var pulseAnimator: ValueAnimator? = null
    private var glowAnimator: ObjectAnimator? = null
    private var currentGlowRadius = 0f
    private var currentPulseScale = 1f

    init {
        setupPaints()
    }

    private fun setupPaints() {
        // Normal eye color (dark)
        eyePaint.color = ContextCompat.getColor(context, android.R.color.black)
        eyePaint.style = Paint.Style.FILL

        // Glow paint for listening state
        glowPaint.color = ContextCompat.getColor(context, R.color.baymax_accent)
        glowPaint.style = Paint.Style.FILL
        glowPaint.alpha = 100
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val baseRadius = (width.coerceAtMost(height) / 2f) * 0.8f

        // Draw glow effect when listening
        if (isListening && currentGlowRadius > 0) {
            val glowGradient = RadialGradient(
                centerX, centerY, currentGlowRadius,
                ContextCompat.getColor(context, R.color.baymax_accent),
                ContextCompat.getColor(context, android.R.color.transparent),
                Shader.TileMode.CLAMP
            )
            glowPaint.shader = glowGradient
            canvas.drawCircle(centerX, centerY, currentGlowRadius, glowPaint)
        }

        // Draw main eye with pulse effect
        val eyeRadius = baseRadius * currentPulseScale
        canvas.drawCircle(centerX, centerY, eyeRadius, eyePaint)
    }

    fun startListening() {
        if (isListening) return
        isListening = true

        // Start pulse animation
        startPulseAnimation()
        startGlowAnimation()
    }

    fun stopListening() {
        if (!isListening) return
        isListening = false

        // Stop animations and reset
        stopPulseAnimation()
        stopGlowAnimation()
    }

    private fun startPulseAnimation() {
        pulseAnimator?.cancel()
        
        pulseAnimator = ValueAnimator.ofFloat(1f, 1.3f, 1f).apply {
            duration = 800
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            
            addUpdateListener { animation ->
                currentPulseScale = animation.animatedValue as Float
                invalidate()
            }
        }
        
        pulseAnimator?.start()
    }

    private fun stopPulseAnimation() {
        pulseAnimator?.cancel()
        pulseAnimator = null
        
        // Animate back to normal size
        val resetAnimator = ValueAnimator.ofFloat(currentPulseScale, 1f).apply {
            duration = 200
            interpolator = AccelerateDecelerateInterpolator()
            
            addUpdateListener { animation ->
                currentPulseScale = animation.animatedValue as Float
                invalidate()
            }
        }
        resetAnimator.start()
    }

    private fun startGlowAnimation() {
        glowAnimator?.cancel()
        
        glowAnimator = ObjectAnimator.ofFloat(0f, 1f, 0f).apply {
            duration = 1200
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            
            addUpdateListener { animation ->
                val progress = animation.animatedValue as Float
                currentGlowRadius = (width.coerceAtMost(height) / 2f) * progress * 1.5f
                invalidate()
            }
        }
        
        glowAnimator?.start()
    }

    private fun stopGlowAnimation() {
        glowAnimator?.cancel()
        glowAnimator = null
        
        // Animate glow to zero
        val fadeOutAnimator = ObjectAnimator.ofFloat(currentGlowRadius, 0f).apply {
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
            
            addUpdateListener { animation ->
                currentGlowRadius = animation.animatedValue as Float
                invalidate()
            }
            
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    currentGlowRadius = 0f
                    invalidate()
                }
            })
        }
        fadeOutAnimator.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopListening()
    }
}
