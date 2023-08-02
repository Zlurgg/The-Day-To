package com.example.thedayto

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.thedayto.ui.theme.gray
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import com.example.thedayto.ui.theme.orange
import kotlinx.coroutines.launch


@Composable
fun Calender(
    modifier: Modifier = Modifier,
    calendarInput: List<CalendarInput>,
    onDayClick:(Int)->Unit,
    strokeWidth:Float = 15f,
    month:String,
    status: Status,
    onReturnButtonClicked: () -> Unit
) {

    val columns = 7
    /** calculate the number of rows needed for the current month **/
    val rows: Int = if (DateUtil().getNumberOfDaysInCurrentMonth() % 7 != 0) {
        (DateUtil().getNumberOfDaysInCurrentMonth() / 7) + 1
    } else {
        DateUtil().getNumberOfDaysInCurrentMonth() / 7
    }


    var canvasSize by remember {
        mutableStateOf(Size.Zero)
    }
    var clickAnimationOffset by remember {
        mutableStateOf(Offset.Zero)
    }

    var animationRadius by remember {
        mutableStateOf(0f)
    }

    val scope = rememberCoroutineScope()

    val ctx = LocalContext.current


    Column() {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            androidx.compose.material.Text(
                text = month,
                fontWeight = FontWeight.SemiBold,
                color = gray,
                fontSize = 40.sp
            )
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(true) {
                        detectTapGestures(
                            onTap = { offset ->
                                val column =
                                    (offset.x / canvasSize.width * columns).toInt() + 1
                                val row = (offset.y / canvasSize.height * rows).toInt() + 1
                                val day = column + (row - 1) * columns
                                if (day <= calendarInput.size) {
                                    onDayClick(day)
                                    clickAnimationOffset = offset

                                    /** animation for calender squares
                                     */
                                    scope.launch {
                                        animate(0f, 225f, animationSpec = tween(300)) { value, _ ->
                                            animationRadius = value
                                        }
                                    }
                                }

                            }
                        )
                    }
            ){
                val canvasHeight = size.height
                val canvasWidth = size.width
                canvasSize = Size(canvasWidth,canvasHeight)
                val ySteps = canvasHeight/ rows
                val xSteps = canvasWidth/ columns

                val column = (clickAnimationOffset.x / canvasSize.width * columns).toInt() + 1
                val row = (clickAnimationOffset.y / canvasSize.height * rows).toInt() + 1

                val path = Path().apply {
                    moveTo((column-1)*xSteps,(row-1)*ySteps)
                    lineTo(column*xSteps,(row-1)*ySteps)
                    lineTo(column*xSteps,row*ySteps)
                    lineTo((column-1)*xSteps,row*ySteps)
                    close()
                }

                clipPath(path){
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(orange.copy(0.8f), orange.copy(0.2f)),
                            center = clickAnimationOffset,
                            radius = animationRadius + 0.1f
                        ),
                        radius = animationRadius + 0.1f,
                        center = clickAnimationOffset
                    )
                }

                drawRoundRect(
                    orange,
                    cornerRadius = CornerRadius(25f,25f),
                    style = Stroke(
                        width = strokeWidth
                    )
                )

                for(i in 1 until rows){
                    drawLine(
                        color = orange,
                        start = Offset(0f,ySteps*i),
                        end = Offset(canvasWidth, ySteps*i),
                        strokeWidth = strokeWidth
                    )
                }
                for(i in 1 until columns){
                    drawLine(
                        color = orange,
                        start = Offset(xSteps*i,0f),
                        end = Offset(xSteps*i, canvasHeight),
                        strokeWidth = strokeWidth
                    )
                }
                val textHeight = 17.dp.toPx()
                for(i in calendarInput.indices){
                    val textPositionX = xSteps * (i% columns) + strokeWidth
                    val textPositionY = (i / columns) * ySteps + textHeight + strokeWidth/2

                    val mood = status.mood

                    drawContext.canvas.nativeCanvas.apply {
                        /** Put number in the calender **/
                        drawText(
                            "${i+1}",
                            textPositionX,
                            textPositionY,
                            Paint().apply {
                                textSize = textHeight
                                color = gray.toArgb()
                                isFakeBoldText = true
                            }
                        )
                        drawImage(
                            topLeft = Offset(x = textPositionX+40, y = textPositionY-15),
                            image = getBitmapFromImage(ctx, R.drawable.small_sad_face))
                    }
                }
            }
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onReturnButtonClicked() }
        ) {
            Text("Home")
        }
    }

}


data class CalendarInput(
    val day:Int,
    val toDos:List<String> = emptyList()
)

fun createCalendarList(): List<CalendarInput> {
    val calendarInputs = mutableListOf<CalendarInput>()
    for (i in 1..31) {
        calendarInputs.add(
            CalendarInput(
                i,
                toDos = listOf(
                    "Day $i:",
                )
            )
        )
    }
    return calendarInputs
}

// on below line we are creating a function to get bitmap
// from image and passing params as context and an int for drawable.
private fun getBitmapFromImage(context: Context, drawable: Int): ImageBitmap {

    // on below line we are getting drawable
    val db = ContextCompat.getDrawable(context, drawable)

    // in below line we are creating our bitmap and initializing it.
    val bit = Bitmap.createBitmap(
        db!!.intrinsicWidth, db.intrinsicHeight, Bitmap.Config.ARGB_8888
    )

    // on below line we are
    // creating a variable for canvas.
    val canvas = Canvas(bit)

    // on below line we are setting bounds for our bitmap.
    db.setBounds(0, 0, canvas.width, canvas.height)

    // on below line we are simply
    // calling draw to draw our canvas.
    db.draw(canvas)

    // on below line we are
    // returning our bitmap.
    return bit.asImageBitmap()
}