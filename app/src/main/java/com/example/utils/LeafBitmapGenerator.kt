package com.example.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

object LeafBitmapGenerator {

    enum class PresetType {
        TOMATO_BLIGHT,
        PEPPER_SPOT,
        CORN_DEFICIENCY,
        HEALTHY_BASIL,
        SPIDER_MITE_WEB
    }

    fun generateLeafBitmap(type: PresetType): Bitmap {
        val width = 500
        val height = 500
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw background (soil color or neutral light)
        canvas.drawColor(Color.parseColor("#F5EDDF"))

        val paint = Paint().apply {
            isAntiAlias = true
        }

        // Draw stems
        paint.color = Color.parseColor("#4E342E") // Earth brown stem
        paint.strokeWidth = 10f
        canvas.drawLine(250f, 450f, 250f, 150f, paint)

        // Draw main leaf body depending on category
        when (type) {
            PresetType.TOMATO_BLIGHT -> {
                // Draw tomato leaf (serrated edges, deep forest green)
                paint.color = Color.parseColor("#2E7D32")
                paint.style = Paint.Style.FILL

                // Main leaf oval
                canvas.drawOval(150f, 180f, 350f, 350f, paint)
                // Side lobes
                canvas.drawOval(90f, 240f, 230f, 325f, paint)
                canvas.drawOval(270f, 240f, 410f, 325f, paint)

                // Blight concentric disease spots (Dark concentric circles)
                paint.style = Paint.Style.FILL
                // Spot 1
                paint.color = Color.parseColor("#3E2723") // dark brown
                canvas.drawCircle(200f, 230f, 20f, paint)
                paint.color = Color.parseColor("#D84315") // orange border
                canvas.drawCircle(200f, 230f, 12f, paint)
                paint.color = Color.parseColor("#000000") // center spot
                canvas.drawCircle(200f, 230f, 6f, paint)

                // Spot 2
                paint.color = Color.parseColor("#3E2723")
                canvas.drawCircle(300f, 280f, 25f, paint)
                paint.color = Color.parseColor("#EF6C00")
                canvas.drawCircle(300f, 280f, 15f, paint)
                paint.color = Color.parseColor("#1A0C00")
                canvas.drawCircle(300f, 280f, 8f, paint)

                // Yellow halo surrounding spot
                paint.color = Color.argb(120, 255, 235, 59)
                canvas.drawCircle(300f, 280f, 40f, paint)
            }
            PresetType.PEPPER_SPOT -> {
                // Pepper leaf (long pointed oval, bright green)
                paint.color = Color.parseColor("#43A047")
                paint.style = Paint.Style.FILL
                
                // Pointy leaf shape via drawing circles overlapping
                canvas.drawOval(180f, 140f, 320f, 390f, paint)
                canvas.drawOval(210f, 100f, 290f, 220f, paint)

                // Bacterial spots (small dark water-soaked pimples)
                paint.color = Color.parseColor("#1B5E20") // very dark leaf spot
                canvas.drawCircle(250f, 180f, 6f, paint)
                canvas.drawCircle(220f, 220f, 5f, paint)
                canvas.drawCircle(280f, 250f, 8f, paint)
                canvas.drawCircle(240f, 310f, 7f, paint)

                // Some yellow specks
                paint.color = Color.parseColor("#FBC02D")
                canvas.drawCircle(230f, 200f, 5f, paint)
                canvas.drawCircle(270f, 210f, 4f, paint)
                canvas.drawCircle(210f, 280f, 6f, paint)
            }
            PresetType.CORN_DEFICIENCY -> {
                // Corn leaf (long strip, lime yellow-green indicating nutrient depletion)
                paint.color = Color.parseColor("#9E9D24") // lime green yellow
                paint.style = Paint.Style.FILL

                // Long strip
                canvas.drawRect(210f, 80f, 290f, 420f, paint)
                canvas.drawOval(210f, 60f, 290f, 120f, paint)

                // V-shaped yellow stripes (typical nitrogen deficiency)
                paint.color = Color.parseColor("#FBC02D") // Bright Yellow
                paint.strokeWidth = 8f
                paint.style = Paint.Style.STROKE
                canvas.drawLine(250f, 100f, 220f, 180f, paint)
                canvas.drawLine(250f, 100f, 280f, 180f, paint)

                canvas.drawLine(250f, 180f, 220f, 260f, paint)
                canvas.drawLine(250f, 180f, 280f, 260f, paint)

                canvas.drawLine(250f, 260f, 225f, 340f, paint)
                canvas.drawLine(250f, 260f, 275f, 340f, paint)
            }
            PresetType.HEALTHY_BASIL -> {
                // Healthy bright emerald leafy basil (pure deep organic green, spotless)
                paint.color = Color.parseColor("#1B5E20")
                paint.style = Paint.Style.FILL

                // Broad healthy leaves
                canvas.drawOval(160f, 150f, 340f, 350f, paint)
                canvas.drawOval(110f, 200f, 260f, 310f, paint)
                canvas.drawOval(240f, 200f, 390f, 310f, paint)

                // White shiny sheen overlay reflecting good moisture
                paint.color = Color.argb(30, 255, 255, 255)
                canvas.drawOval(180f, 170f, 260f, 230f, paint)
            }
            PresetType.SPIDER_MITE_WEB -> {
                // General leaves with grayish mite webs
                paint.color = Color.parseColor("#558B2F") // Dull olive green
                paint.style = Paint.Style.FILL

                // Leaves
                canvas.drawOval(140f, 180f, 360f, 340f, paint)

                // Yellow stipples (speckled sap marks)
                paint.color = Color.parseColor("#DCEDC8") // extremely pale yellowish green
                paint.style = Paint.Style.FILL
                for (i in 0..15) {
                    val px = (160..340).random().toFloat()
                    val py = (200..320).random().toFloat()
                    canvas.drawCircle(px, py, 4f, paint)
                }

                // Fine gray protective spider webs (lines drawn across)
                paint.color = Color.argb(160, 230, 230, 230) // semi-transparent web structure
                paint.strokeWidth = 3f
                paint.style = Paint.Style.STROKE
                
                canvas.drawLine(150f, 200f, 350f, 320f, paint)
                canvas.drawLine(350f, 200f, 150f, 320f, paint)
                canvas.drawLine(250f, 150f, 250f, 350f, paint)
                canvas.drawLine(130f, 250f, 370f, 250f, paint)

                // Web circles
                canvas.drawCircle(250f, 250f, 40f, paint)
                canvas.drawCircle(250f, 250f, 80f, paint)
            }
        }

        return bitmap
    }
}
