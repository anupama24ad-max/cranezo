package com.cranezo.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.cranezo.R
import com.cranezo.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged

/*val InterFontFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold)
)*/

fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "")
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000)
        ), label = ""
    )

    background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFE0E0E0),
                Color(0xFFF5F5F5),
                Color(0xFFE0E0E0),
            ),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
        )
    ).onGloballyPositioned {
        size = it.size
    }
}

@Composable
fun Textview(
    text: String,
    size: TextUnit = font_16,      // e.g., 14.sp, 20.sp
    color: Color = Black,
    fontWeight: FontWeight = FontWeight.Normal,
    italic: Boolean = false,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    isunderline: Boolean = false,
) {
    Text(
        text = text,
        color = color,
        modifier = modifier,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign,
        style = TextStyle(
//            fontFamily = InterFontFamily,
            fontSize = size,
            fontWeight = fontWeight,
            fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
        ),
        textDecoration = if (isunderline) TextDecoration.Underline else TextDecoration.None
    )
}

@Composable
fun TextviewWithGradient(
    text: String,
    size: TextUnit = font_16,      // e.g., 14.sp, 20.sp
    color: List<Color> = listOf(DarkBlue, Grey),
    fontWeight: FontWeight = FontWeight.Normal,
    italic: Boolean = false,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    isunderline: Boolean = false,
    fontFamily: FontFamily? = null
) {
    Text(
        text = text,
        modifier = modifier,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign,
        style = TextStyle(
            fontSize = size,
            brush = Brush.verticalGradient(color),
            fontWeight = fontWeight,
            fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
//            fontFamily = fontFamily ?: InterFontFamily
        ),
        textDecoration = if (isunderline) TextDecoration.Underline else TextDecoration.None
    )
}


@Composable
fun LabeledField(
    @StringRes labelRes: Int? = null,
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes placeholderRes: Int,
    modifier: Modifier = Modifier,
    // Label styling
    labelSize: TextUnit = font_14,
    labelBold: Boolean = false,
    labelMedium: Boolean = false,
    labelSemiBold: Boolean = false,
    labelColor: Color = DarkGrey,
    // Spacing
    labelToFieldSpace: Dp = dimen_5,
    bottomSpace: Dp = dimen_20,
    // AppEditText passthroughs
    isPassword: Boolean = false,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {},
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    isPhoneNumber: Boolean = false,
    enabled: Boolean = true,
    height: Dp = dimen_50,
    moreText: Boolean = false,
    isRequired: Boolean = false,
    isLock: Boolean = false,
    leftImage: Int? = null
) {
    if (labelRes != null) {
        val labelText = buildAnnotatedString {
            append(stringResource(labelRes))
            if (isRequired) {
                append(" ")
                withStyle(style = SpanStyle(color = Color.Red)) {
                    append("*")
                }
            }
        }
        LabelTextview(
            text = labelText,
            size = labelSize,
            bold = labelBold,
            medium = labelMedium,
            semiBold = labelSemiBold,
            color = labelColor
        )
        Spacer(Modifier.height(labelToFieldSpace))
    }
    CompactEditText(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        isPassword = isPassword,
        imeAction = imeAction,
        onImeAction = onImeAction,
        placeholder = stringResource(placeholderRes),
        isPhoneNumber = isPhoneNumber,
        enabled = enabled,
        height = height,
        singleLine = singleLine,
        moreText = moreText,
        isLock = isLock,
        leftImage = leftImage
    )

}

@Composable
fun LabelTextview(
    text: AnnotatedString,
    size: TextUnit = font_14,      // e.g., 14.sp, 20.sp
    bold: Boolean = false,
    semiBold: Boolean = false,
    medium: Boolean = false,
    color: Color = Blackish,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
) {
    Text(
        text = text,
        color = color,
        modifier = modifier,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign,
        style = TextStyle(
//            fontFamily = InterFontFamily,
            fontSize = size,
            fontWeight = when {
                bold -> FontWeight.Bold
                semiBold -> FontWeight.SemiBold
                medium -> FontWeight.Medium
                else -> FontWeight.Normal
            }
        ),
    )
}

@Composable
fun CompactEditText(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    imeAction: ImeAction = ImeAction.Done,
    isPhoneNumber: Boolean = false,
    enabled: Boolean = true,
    height: Dp = dimen_50,
    moreText: Boolean = false,
    singleLine: Boolean = true,
    isLock: Boolean = false,
    leftImage: Int? = null,
    onImeAction: () -> Unit = {},

    ) {
    var obscure by rememberSaveable(isPassword) { mutableStateOf(isPassword) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .border(dimen_1, MidGrey, RoundedCornerShape(dimen_8)),
        shape = RoundedCornerShape(dimen_8),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Transparent)
        ) {
            Row(
                modifier = Modifier
                    .padding(
                        start = dimen_12,
                        end = if (isPassword) dimen_0 else dimen_12,
                        top = dimen_8,
                        bottom = dimen_8
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leftImage != null) {
                    Image(
                        painter = painterResource(leftImage), // your image
                        contentDescription = null,
                        modifier = Modifier
                            .size(dimen_22)
                    )
                    Spacer(
                        modifier = Modifier.width(dimen_12)
                    )
                }

                // text field area
                BasicTextField(
                    value = value,
                    onValueChange = {
                        if (isPhoneNumber) {
                            // Allow only digits & max 10 length
                            val digitsOnly = it.filter { it.isDigit() }
                            if (digitsOnly.length <= 10) {
                                onValueChange(digitsOnly)
                            }
                        } else {
                            onValueChange(it)
                        }
                    },
                    singleLine = singleLine,
                    enabled = enabled,
                    textStyle = TextStyle(color = Blackish, font_14),
                    visualTransformation = if (isPassword && obscure) PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (isPassword) KeyboardType.Password else if (isPhoneNumber) KeyboardType.Number else KeyboardType.Text,
                        imeAction = imeAction
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onImeAction() },
                        onNext = { onImeAction() }),
                    cursorBrush = SolidColor(Color.Black),
                    modifier = Modifier
                        .weight(1f)
                ) { inner ->
                    // placeholder + text
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = if (moreText) Alignment.TopStart else Alignment.CenterStart // Center the placeholder text
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                placeholder,
                                color = MidGrey,
                                style = TextStyle(fontSize = font_14)
                            )
                        }
                        inner()
                    }
                }

                if (isPassword) {
                    IconButton(
                        onClick = { obscure = !obscure },
                        modifier = Modifier.padding(end = dimen_0)
                    ) {
                        Image(
                            painter = painterResource(if (obscure) R.drawable.ic_launcher_background else R.drawable.ic_launcher_background),
                            contentDescription = null,
                            modifier = Modifier.size(dimen_20)
                        )
                    }
                }
            }
        }

    }
}

@Composable
fun GradientButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color1: Color = Grey,
    color2: Color = Grey,
    color3: Color? = null,
    textColor: Color = Color.White,
    height: Dp = dimen_50,
    fontWeight: FontWeight = FontWeight.Bold,
    leftImage: Int? = null,
    rightImage: Int? = null,
    textSize: TextUnit = font_16,
    rightIconSize: Dp = dimen_20,
    isCircle : Boolean = true,
    shape: RoundedCornerShape = RoundedCornerShape(dimen_8),
    onClick: () -> Unit,
) {
    val gradientColors = if (!enabled) {
        listOf(
            Grey,
            Grey
        ).map { it.copy(0.5f) }

    } else {

        if (color3 != null) {
            listOf(color1, color2, color3)
        } else {
            listOf(color1, color2)
        }

    }

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp)
    )
    {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(gradientColors),
                    shape = shape
                )
                .padding(horizontal = dimen_10)
        ) {

            // Left Image
            leftImage?.let {
                if (isCircle){
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .size(dimen_40)
                            .background(
                                color = White,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(it),
                            contentDescription = null,
                            modifier = Modifier.size(dimen_24)
                        )
                    }
                }
            }

            // Center Text
            Textview(
                text = text,
                size = textSize,
                fontWeight = fontWeight,
                color = textColor,
                modifier = Modifier.align(Alignment.Center)
            )

            // Right Image
            rightImage?.let {
                Icon(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(rightIconSize),
                    tint = Color.Unspecified
                )
            }
        }
    }
}

@Composable
fun BottomSheetWithTopCorner(
    modifier: Modifier = Modifier.fillMaxSize(),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.linearGradient(listOf(White, White)),
                shape = RoundedCornerShape(topStart = dimen_40, topEnd = dimen_40)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = dimen_24, vertical = dimen_20),
            verticalArrangement = verticalArrangement,
            content = content,
        )
    }
}

@Composable
fun PasswordData(
    title: String,
    des: String,
    des2: String = "",
    image: Painter,
    isDes: Boolean = false,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(start = dimen_20, end = dimen_20, top = dimen_30, bottom = dimen_0)
            .statusBarsPadding()
    )
    {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                /*Image(
                    painter = painterResource(R.drawable.back_ic_with_card),
                    contentDescription = null,
                    modifier = Modifier
                        .size(dimen_38)
                        .clickable {
                            onBackClick()
                        }
                )*/
            }

            Textview(
                text = title,
                color = Blackish,
                size = font_24,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(dimen_14))
        Textview(
            text = des,
            size = font_16,
            italic = true
        )
        if (isDes) {
            Spacer(modifier = Modifier.height(dimen_10))
            Textview(
                text = des2,
                size = font_16,
                italic = true
            )
        }
        Spacer(modifier = Modifier.height(dimen_30))
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = image,
                contentDescription = null,
                modifier = Modifier
                    .width(dimen_244)
                    .height(dimen_340)
            )
        }
    }
}

@Composable
fun OtpView(
    modifier: Modifier = Modifier,
    cells: Int = 6,
    onFilled: (String) -> Unit = {},
    onChanged: (String) -> Unit = {} // <— add this
) {
    var value by rememberSaveable { mutableStateOf("") }

    OtpField(
        value = value,
        onValueChange = {
            val digitsOnly = it.filter(Char::isDigit).take(cells)
            if (digitsOnly != value) {
                value = digitsOnly
                onChanged(digitsOnly)                 // notify parent every change
                if (digitsOnly.length == cells) onFilled(digitsOnly)
            }
        },
        cells = cells,
        modifier = modifier
    )
}

@Composable
fun OtpField(
    value: String,
    onValueChange: (String) -> Unit,
    cells: Int,
    modifier: Modifier = Modifier,
    cellWidth: Dp = dimen_44,
    cellHeight: Dp = dimen_52,
    cornerRadius: Dp = dimen_14,
    gap: Dp = dimen_12,
    placeholderChar: Char = '0'
) {
    val focusRequester = remember { FocusRequester() }
    val bg = White
    val placeholderColor = MidGrey
    val shape = RoundedCornerShape(cornerRadius)

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        cursorBrush = SolidColor(Color.Transparent), // hide cursor
        modifier = modifier
            .focusRequester(focusRequester)
            .onKeyEvent { event ->
                // Make backspace behave nicely when nothing is selected
                if (event.type == KeyEventType.KeyUp && event.key == Key.Backspace && value.isNotEmpty()) {
                    onValueChange(value.dropLast(1))
                    true
                } else false
            },
        decorationBox = { innerTextField ->
            // The invisible editor covers the boxes so it can receive input.
            Box {
                Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                    repeat(cells) { index ->
                        val isActive = index == value.length && value.length < cells
                        val ch = value.getOrNull(index)?.toString() ?: placeholderChar.toString()

                        Box(
                            modifier = Modifier
                                .size(cellWidth, cellHeight)
                                .clip(shape)
                                .background(bg)
                                .border(
                                    width = dimen_1,
                                    color = MidGrey,
                                    shape = shape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ch,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = font_16,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = if (value.getOrNull(index) != null)
                                    Blackish
                                else
                                    placeholderColor
                            )
                        }
                    }
                }
                // Invisible, full-size editor to capture input & paste
                Box(
                    Modifier
                        .matchParentSize()
                        .alpha(0f)
                ) { innerTextField() }
            }
        }
    )
}

@Composable
fun ResendOtp(
    onResendOtp: () -> Unit,
    totalSeconds: Int = 30,
) {
    var secondsLeft by rememberSaveable { mutableIntStateOf(totalSeconds) }
    var timerKey by rememberSaveable { mutableIntStateOf(0) }

    val isEnabled = secondsLeft == 0

    LaunchedEffect(timerKey) {
        secondsLeft = totalSeconds
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
    }

    val annotatedText = buildAnnotatedString {

        // Didn't receive text
        withStyle(
            SpanStyle(
                color = LightGrey,
                fontWeight = FontWeight.Bold,
                fontSize = font_12
            )
        ) {
            append("Didn't receive the code? ")
        }

        // Timer
        withStyle(
            SpanStyle(
                color = if (isEnabled) MidGrey else Red,
                fontSize = font_14
            )
        ) {
            append(formatAsMMSS(secondsLeft))
            append(" ")
        }

        // Resend OTP
        pushStringAnnotation(
            tag = "RESENDOTP",
            annotation = "resendotp"
        )

        withStyle(
            SpanStyle(
                color = if (isEnabled) Red else MidGrey,
                fontWeight = FontWeight.SemiBold,
                fontSize = font_14,
            )
        ) {
            append("Resend OTP")
        }

        pop()
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ClickableText(
            text = annotatedText,
            style = MaterialTheme.typography.bodyMedium,
            onClick = { offset ->
                if (!isEnabled) return@ClickableText

                annotatedText.getStringAnnotations(
                    tag = "RESENDOTP",
                    start = offset,
                    end = offset
                ).firstOrNull()?.let {
                    onResendOtp()
                    timerKey++
                }
            }
        )
    }
}

fun formatAsMMSS(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}

@Composable
fun VerticalDashedLine(
    modifier: Modifier = Modifier,
    color: Color = Color.Gray,
    strokeWidth: Dp = 2.dp,
    dashLength: Dp = 6.dp,
    gapLength: Dp = 4.dp
) {
    Canvas(
        modifier = modifier
            .fillMaxHeight() // Takes up full height unless overridden in modifier
            .width(strokeWidth) // Ensures the canvas is exactly as wide as the line
    ) {
        // Convert Dp values to Pixels for the drawing canvas
        val strokeWidthPx = strokeWidth.toPx()
        val dashLengthPx = dashLength.toPx()
        val gapLengthPx = gapLength.toPx()

        // Define the dash and gap sequence
        val pathEffect = PathEffect.dashPathEffect(
            intervals = floatArrayOf(dashLengthPx, gapLengthPx),
            phase = 0f
        )

        // Draw the vertical line from top to bottom
        drawLine(
            color = color,
            start = Offset(x = size.width / 2, y = 0f),
            end = Offset(x = size.width / 2, y = size.height),
            strokeWidth = strokeWidthPx,
            pathEffect = pathEffect
        )
    }
}

@Composable
fun InfoItem(
    icon: Int,
    text: String,
    text2: String? = null,
    bgColor: Color = SkyBlue,
    modifier: Modifier = Modifier,
    titleSize: TextUnit = font_12,
    desSize: TextUnit = font_12,
    titleFontWeight: FontWeight = FontWeight.Medium,
    desFontWeight: FontWeight = FontWeight.SemiBold,
    desColor: Color = DarkBlack,
    size: Dp = dimen_28,
    imageSize: Dp = dimen_14,
    titleColor: List<Color> = listOf(Grey, DarkBlue)
) {

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(size)
                .background(
                    color = bgColor,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {

            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(imageSize)
            )
        }

        Spacer(Modifier.width(dimen_6))
        Column {
            TextviewWithGradient(
                text = text,
                size = titleSize,
                color = titleColor,
                fontWeight = titleFontWeight,
                modifier = Modifier.padding(bottom = dimen_2)
            )
            if (!text2.isNullOrEmpty()) {
                Textview(
                    text = text2,
                    size = desSize,
                    fontWeight = desFontWeight,
                    color = desColor
                )
            }


        }

    }
}


@Composable
fun PendingData(
    bgColor: List<Color> = listOf(LightSkyBluish, White),
    image: Int,
    title: String,
    des: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(bgColor),
                shape = RoundedCornerShape(dimen_9)
            )
    )
    {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimen_12)
        ) {
            Box(
                modifier = Modifier
                    .size(dimen_34)
                    .background(
                        color = MidSkyBlue,
                        shape = RoundedCornerShape(dimen_9)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(image),
                    contentDescription = null,
                    modifier = Modifier.size(dimen_30)
                )
            }
            Column(
                modifier = Modifier
                    .padding(start = dimen_8)
                    .weight(1f)
            ) {
                Textview(text = title, size = font_12, color = DarkGreyish)
                Textview(
                    text = des,
                    size = font_16,
                    color = BlackGrey,
                    fontWeight = FontWeight.Medium
                )
            }
        }


    }
}


@Composable
fun <T : Any> LazyColumnListViewMinMax(
    items: List<T>,
    key: ((T) -> Any)? = null,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalSpacing: Dp = 8.dp,
    totalCount: Int,                 // ✅ total records count from API
    min: Int,                 // ✅ current page number
    max: Int,                   // ✅ current page size
    onLoadMore: (nextPage: Int) -> Unit,  // ✅ callback for pagination
    isLoading: Boolean = false,
    prefetchDistance: Int = 2,       // ✅ trigger when last 2 visible
    state: LazyListState = rememberLazyListState(),
    itemContent: @Composable (item: T) -> Unit
) {
    // ✅ Scroll listener for pagination
    LaunchedEffect(items.size, isLoading, min, totalCount) {
        snapshotFlow {
            val info = state.layoutInfo
            val totalItems = info.totalItemsCount
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: -1

            // true when near the end
            totalItems > 0 && lastVisible >= totalItems - 1 - prefetchDistance
        }
            .distinctUntilChanged()
            .collect { nearEnd ->
                if (
                    nearEnd &&
                    !isLoading &&
                    items.size < totalCount // still have more data
                ) {
                    val nextPage = min + 10
                    onLoadMore(nextPage)
                }
            }
    }

    if (items.isEmpty() && isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
         /*   LottiePlayer(
                lottieRes = R.raw.app_loader,
                modifier = Modifier.size(150.dp),
                loop = true,
                speed = 1f
            )*/
        }
    } else {
        LazyColumn(
            state = state,
            modifier = modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(verticalSpacing)
        )
        {
            itemsIndexed(
                items,
                key = { index, item -> key?.invoke(item) ?: "${item.hashCode()}_$index" }) { _, item ->
                itemContent(item)
            }

            // ✅ Optional footer loader
            if (isLoading) {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = dimen_16)
                            .background(Transparent)
                            .clickable(false) {},
                        contentAlignment = Alignment.Center
                    ) {
                        /*LottiePlayer(
                            lottieRes = R.raw.app_loader,
                            modifier = Modifier.size(80.dp),
                            loop = true,
                            speed = 1f
                        )*/
                    }
                }
            }
        }
    }
}


@Composable
fun HeaderWithBackIcon(
    title: String,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {

        Card(
            modifier = Modifier
                .size(dimen_38)
                .clip(RoundedCornerShape(dimen_12))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onBackClick() },
            shape = RoundedCornerShape(dimen_12),
            colors = CardDefaults.cardColors(
                containerColor = White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = dimen_2
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
               /* Image(
                    painter = painterResource(R.drawable.back_arrow_ic),
                    contentDescription = null,
                    modifier = Modifier.size(dimen_10)
                )*/
            }
        }



        Textview(
            text = title,
            size = font_18,
            color = BlackGrey,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmCategoryBottomSheet(
    categoryName: String,
    imageRes: Int,
    sheetState: androidx.compose.material3.SheetState,
    onDismiss: () -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = White,
        shape = RoundedCornerShape(topStart = dimen_24, topEnd = dimen_24)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimen_24, vertical = dimen_16),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = null,
                modifier = Modifier.size(dimen_80)
            )

            Spacer(modifier = Modifier.height(dimen_16))

            // Title
            Textview(
                text = "Are you sure?",
                size = font_20,
                fontWeight = FontWeight.Bold,
                color = Blackish
            )

            Spacer(modifier = Modifier.height(dimen_8))

            // Description
            Textview(
                text = "Do you want to view items under $categoryName?",
                size = font_14,
                color = DarkGreyish,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(dimen_24))

            // Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimen_12)
            ) {
                // Cancel Button
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .height(dimen_48),
                    shape = RoundedCornerShape(dimen_12),
                    border = androidx.compose.foundation.BorderStroke(dimen_1, MidGrey)
                ) {
                    Textview(
                        text = "Cancel",
                        size = font_16,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkGreyish
                    )
                }

                // OK Button
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .weight(1f)
                        .height(dimen_48),
                    shape = RoundedCornerShape(dimen_12),
                    colors = ButtonDefaults.buttonColors(containerColor = Green)
                ) {
                    Textview(
                        text = "OK",
                        size = font_16,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimen_20))
        }
    }
}

@Composable
fun DashedLine(
    modifier: Modifier = Modifier
) {

    Canvas(
        modifier = modifier
            .height(dimen_2)
    ) {

        drawLine(
            color = MidLightGreen,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 3f,
            pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(8f, 8f),
                0f
            )
        )
    }
}

@Composable
fun CompactEditTextForDes(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    imeAction: ImeAction = ImeAction.Done,
    isPhoneNumber: Boolean = false,
    enabled: Boolean = true,
    height: Dp = dimen_50,
    moreText: Boolean = false,
    singleLine: Boolean = true,
    isLock: Boolean = false,
    leftImage: Int? = null,
    onImeAction: () -> Unit = {},

    ) {
    var obscure by rememberSaveable(isPassword) { mutableStateOf(isPassword) }


    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .border(dimen_1, MidGrey, RoundedCornerShape(dimen_8)),
        shape = RoundedCornerShape(dimen_8),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Transparent)
        ) {
            Row(
                modifier = Modifier
                    .padding(
                        start = dimen_12,
                        end = if (isPassword) dimen_0 else dimen_12,
                        top = dimen_8,
                        bottom = dimen_8
                    ),
                verticalAlignment = Alignment.Top
            ) {
                if (leftImage != null) {
                    Image(
                        painter = painterResource(leftImage), // your image
                        contentDescription = null,
                        modifier = Modifier
                            .size(dimen_22)
                    )
                    Spacer(
                        modifier = Modifier.width(dimen_12)
                    )
                }

                // text field area
                BasicTextField(
                    value = value,
                    onValueChange = {
                        if (isPhoneNumber) {
                            // Allow only digits & max 10 length
                            val digitsOnly = it.filter { it.isDigit() }
                            if (digitsOnly.length <= 10) {
                                onValueChange(digitsOnly)
                            }
                        } else {
                            onValueChange(it)
                        }
                    },
                    singleLine = singleLine,
                    enabled = enabled,
                    textStyle = TextStyle(color = Blackish, font_14),
                    visualTransformation = if (isPassword && obscure) PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (isPassword) KeyboardType.Password else if (isPhoneNumber) KeyboardType.Number else KeyboardType.Text,
                        imeAction = imeAction
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onImeAction() },
                        onNext = { onImeAction() }),
                    cursorBrush = SolidColor(Color.Black),
                    modifier = Modifier
                        .weight(1f)
                ) { inner ->
                    // placeholder + text
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = if (moreText) Alignment.TopStart else Alignment.CenterStart // Center the placeholder text
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                placeholder,
                                color = MidGrey,
                                style = TextStyle(fontSize = font_14)
                            )
                        }
                        inner()
                    }
                }

                if (isPassword) {
                    IconButton(
                        onClick = { obscure = !obscure },
                        modifier = Modifier.padding(end = dimen_0)
                    ) {
                        Image(
                            painter = painterResource(if (obscure) R.drawable.ic_launcher_background else R.drawable.ic_launcher_background),
                            contentDescription = null,
                            modifier = Modifier.size(dimen_20)
                        )
                    }
                }
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogoutBottomSheet(
    title : String,
    subtitle : String,
    onCancel: () -> Unit,
    onLogout: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(
            topStart = dimen_42,
            topEnd = dimen_42
        ),
        containerColor = Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        topStart = dimen_42,
                        topEnd = dimen_42
                    )
                )
                .background(White)
                .padding(
                    dimen_24
                )
        )
        {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Textview(
                    text = title,
                    size = font_16,
                    fontWeight = FontWeight.SemiBold,
                    color = Black
                )

                Spacer(modifier = Modifier.height(dimen_10))
                Textview(
                    text = subtitle,
                    size = font_14,
                    color = Black,
                    textAlign = TextAlign.Center,
                )


                Spacer(modifier = Modifier.height(dimen_26))


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimen_12)
                ) {

                    // Cancel Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(dimen_46)
                            .clip(RoundedCornerShape(dimen_24))
                            .border(
                                width = dimen_1,
                                color = WhitishGrey,
                                shape = RoundedCornerShape(dimen_24)
                            )
                            .clickable { onCancel() },
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = "Cancel",
                            fontSize = font_14,
                            fontWeight = FontWeight.Bold,
                            color = Black
                        )
                    }


                    // Logout Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(dimen_46)
                            .clip(RoundedCornerShape(dimen_24))
                            .background(Red, shape = RoundedCornerShape(dimen_24))
                            .clickable { onLogout() },
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = title,
                            fontSize = font_14,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                    }
                }
            }
        }
    }

}

@Composable
fun LottiePlayer(
    lottieRes: Int, modifier: Modifier = Modifier, loop: Boolean = true, speed: Float = 1f
) {
    // Load the Lottie composition
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieRes))

    // Animate the composition
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = if (loop) LottieConstants.IterateForever else 1,
        isPlaying = true,
        speed = speed
    )

    // Display Lottie animation
    LottieAnimation(
        composition = composition,
        progress = progress,
        modifier = modifier,
        contentScale = ContentScale.FillBounds // This removes extra padding
    )
}

@Composable
fun <T : Any> LazyGridListViewMinMax(
    items: List<T>,
    columns: Int = 3,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(dimen_0),
    horizontalSpacing: Dp = dimen_12,
    verticalSpacing: Dp = dimen_12,
    totalCount: Int,
    min: Int,
    max: Int,
    isLoading: Boolean = false,
    prefetchDistance: Int = 3,
    onLoadMore: (Int) -> Unit,
    itemContent: @Composable (T) -> Unit
) {
    val gridState = rememberLazyGridState()

    LaunchedEffect(items.size, isLoading, min, totalCount) {
        snapshotFlow {
            val layoutInfo = gridState.layoutInfo
            val lastVisible =
                layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1

            lastVisible >= layoutInfo.totalItemsCount - 1 - prefetchDistance
        }
            .distinctUntilChanged()
            .collect { shouldLoad ->

                if (
                    shouldLoad &&
                    !isLoading &&
                    items.size < totalCount
                ) {
                    onLoadMore(min + max)
                }
            }
    }

    if (items.isEmpty() && isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            /*LottiePlayer(
                lottieRes = R.raw.app_loader,
                modifier = Modifier.size(150.dp),
                loop = true,
                speed = 1f
            )*/
        }
    } else {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(columns),
            modifier = modifier,
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing)
        ) {

            items(items) { item ->
                itemContent(item)
            }

            if (isLoading) {
                item(span = { GridItemSpan(columns) }) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = dimen_16)
                            .background(Transparent)
                            .clickable(false) {},
                        contentAlignment = Alignment.Center
                    ) {
                        /*LottiePlayer(
                            lottieRes = R.raw.app_loader,
                            modifier = Modifier.size(80.dp),
                            loop = true,
                            speed = 1f
                        )*/
                    }
                }
            }
        }
    }
}