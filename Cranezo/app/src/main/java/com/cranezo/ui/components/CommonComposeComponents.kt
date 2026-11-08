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
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.Color.Companion.Transparent
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
import androidx.compose.animation.core.RepeatMode
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

/** Moving highlight used while something loads (images, placeholders). Follows light / dark mode. */
fun Modifier.shimmerEffect(): Modifier = composed {
    val dark = appColors.isDark
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "")
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1100)
        ), label = ""
    )

    background(
        brush = Brush.linearGradient(
            colors = if (dark) listOf(Color(0xFF1A2333), Color(0xFF2A3548), Color(0xFF1A2333))
            else listOf(Color(0xFFE0E0E0), Color(0xFFF5F5F5), Color(0xFFE0E0E0)),
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
    fontFamily: FontFamily = GeistFontFamily
) {
    Text(
        text = text,
        color = color,
        modifier = modifier,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign,
        style = TextStyle(
            fontFamily = fontFamily,
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
    color: List<Color> = listOf(LightYellow, DarkYellow),
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
            fontFamily = fontFamily ?: GeistFontFamily
        ),
        textDecoration = if (isunderline) TextDecoration.Underline else TextDecoration.None
    )
}


@Composable
fun LabeledField(
    @StringRes labelRes: Int? = null,
    labelText: String? = null,
    subLabel: String? = null,
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes placeholderRes: Int = 0,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    // Label styling
    labelSize: TextUnit = font_14,
    labelBold: Boolean = false,
    labelMedium: Boolean = false,
    labelSemiBold: Boolean = false,
    labelColor: Color = DarkGrey,
    // Spacing
    labelToFieldSpace: Dp = dimen_8,
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
    height: Dp = dimen_52,
    moreText: Boolean = false,
    isRequired: Boolean = false,
    isLock: Boolean = false,
    leftImage: Int? = null,
    rightImage: Int? = null,
    rightContent: (@Composable () -> Unit)? = null,
    readOnly: Boolean = false,
    // Glassy styling & country code
    isGlassy: Boolean = false,
    isPhoneWithCountry: Boolean = false,
    countryCode: String = "+91",
    countryFlag: String = "🇮🇳",
    onCountryCodeClick: (() -> Unit)? = null
) {
    val resolvedLabel = when {
        labelRes != null -> stringResource(labelRes)
        labelText != null -> labelText
        else -> null
    }

    val resolvedPlaceholder = when {
        placeholder.isNotEmpty() -> placeholder
        placeholderRes != 0 -> stringResource(placeholderRes)
        else -> ""
    }

    Column(modifier = modifier.fillMaxWidth()) {
        if (resolvedLabel != null || subLabel != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = labelToFieldSpace),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (resolvedLabel != null) {
                    val labelAnnotated = buildAnnotatedString {
                        append(resolvedLabel)
                        if (isRequired) {
                            append(" ")
                            withStyle(style = SpanStyle(color = Color.Red)) {
                                append("*")
                            }
                        }
                    }
                    LabelTextview(
                        text = labelAnnotated,
                        size = labelSize,
                        bold = labelBold,
                        medium = labelMedium,
                        semiBold = labelSemiBold || isGlassy,
                        color = if (isGlassy) appColors.textPrimary else labelColor
                    )
                } else {
                    Spacer(modifier = Modifier.width(dimen_0))
                }

                if (subLabel != null) {
                    Textview(
                        text = subLabel,
                        size = font_12,
                        color = SubtitleGrey,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }

        if (isGlassy) {
            val fieldShape = RoundedCornerShape(dimen_14)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .glassEffect(fieldShape)
                    .padding(horizontal = dimen_12),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isPhoneWithCountry) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(dimen_6))
                                .clickable(enabled = onCountryCodeClick != null) { onCountryCodeClick?.invoke() }
                                .padding(vertical = dimen_4, horizontal = dimen_4),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Textview(
                                text = countryFlag,
                                size = font_16
                            )
                            Spacer(modifier = Modifier.width(dimen_4))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = appColors.textSecondary,
                                modifier = Modifier.size(dimen_16)
                            )
                            Spacer(modifier = Modifier.width(dimen_6))
                            Textview(
                                text = countryCode,
                                size = font_14,
                                color = appColors.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(dimen_10))

                        // Vertical divider
                        Box(
                            modifier = Modifier
                                .width(dimen_1)
                                .height(dimen_24)
                                .background(appColors.subtleBorder)
                        )

                        Spacer(modifier = Modifier.width(dimen_12))
                    } else if (leftImage != null) {
                        Image(
                            painter = painterResource(leftImage),
                            contentDescription = null,
                            modifier = Modifier.size(dimen_22)
                        )
                        Spacer(modifier = Modifier.width(dimen_12))
                    }

                    // BasicTextField
                    BasicTextField(
                        value = value,
                        onValueChange = { input ->
                            if (!readOnly) {
                                if (isPhoneNumber || isPhoneWithCountry) {
                                    val digitsOnly = input.filter { it.isDigit() }
                                    if (digitsOnly.length <= 10) {
                                        onValueChange(digitsOnly)
                                    }
                                } else {
                                    onValueChange(input)
                                }
                            }
                        },
                        readOnly = readOnly,
                        modifier = Modifier.weight(1f),
                        singleLine = singleLine,
                        enabled = enabled,
                        keyboardOptions = if (isPhoneNumber || isPhoneWithCountry) {
                            KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = imeAction
                            )
                        } else keyboardOptions,
                        keyboardActions = KeyboardActions(
                            onDone = { onImeAction() },
                            onNext = { onImeAction() }),
                        textStyle = TextStyle(
                            fontSize = font_15,
                            color = appColors.textPrimary,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(AmberPrimary),
                        decorationBox = { innerTextField ->
                            if (value.isEmpty()) {
                                Textview(
                                    text = resolvedPlaceholder,
                                    size = font_14,
                                    color = appColors.textHint
                                )
                            }
                            innerTextField()
                        }
                    )

                    if (rightContent != null) {
                        Spacer(modifier = Modifier.width(dimen_8))
                        rightContent()
                    } else if (rightImage != null) {
                        Spacer(modifier = Modifier.width(dimen_8))
                        Image(
                            painter = painterResource(rightImage),
                            contentDescription = null,
                            modifier = Modifier.size(dimen_20)
                        )
                    }
                }
            }
        } else {
            CompactEditText(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                isPassword = isPassword,
                imeAction = imeAction,
                onImeAction = onImeAction,
                placeholder = resolvedPlaceholder,
                isPhoneNumber = isPhoneNumber,
                enabled = enabled,
                height = height,
                singleLine = singleLine,
                moreText = moreText,
                isLock = isLock,
                leftImage = leftImage
            )
        }
    }
}

/**
 * Convenience alias for [LabeledField]
 */
@Composable
fun LabelTextField(
    @StringRes labelRes: Int? = null,
    labelText: String? = null,
    subLabel: String? = null,
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes placeholderRes: Int = 0,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    labelSize: TextUnit = font_14,
    labelBold: Boolean = false,
    labelMedium: Boolean = false,
    labelSemiBold: Boolean = false,
    labelColor: Color = DarkGrey,
    labelToFieldSpace: Dp = dimen_8,
    bottomSpace: Dp = dimen_20,
    isPassword: Boolean = false,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {},
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    isPhoneNumber: Boolean = false,
    enabled: Boolean = true,
    height: Dp = dimen_52,
    moreText: Boolean = false,
    isRequired: Boolean = false,
    isLock: Boolean = false,
    leftImage: Int? = null,
    rightImage: Int? = null,
    rightContent: (@Composable () -> Unit)? = null,
    readOnly: Boolean = false,
    isGlassy: Boolean = false,
    isPhoneWithCountry: Boolean = false,
    countryCode: String = "+91",
    countryFlag: String = "🇮🇳",
    onCountryCodeClick: (() -> Unit)? = null
) {
    LabeledField(
        labelRes = labelRes,
        labelText = labelText,
        subLabel = subLabel,
        value = value,
        onValueChange = onValueChange,
        placeholderRes = placeholderRes,
        placeholder = placeholder,
        modifier = modifier,
        labelSize = labelSize,
        labelBold = labelBold,
        labelMedium = labelMedium,
        labelSemiBold = labelSemiBold,
        labelColor = labelColor,
        labelToFieldSpace = labelToFieldSpace,
        bottomSpace = bottomSpace,
        isPassword = isPassword,
        imeAction = imeAction,
        onImeAction = onImeAction,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        singleLine = singleLine,
        isPhoneNumber = isPhoneNumber,
        enabled = enabled,
        height = height,
        moreText = moreText,
        isRequired = isRequired,
        isLock = isLock,
        leftImage = leftImage,
        rightImage = rightImage,
        rightContent = rightContent,
        readOnly = readOnly,
        isGlassy = isGlassy,
        isPhoneWithCountry = isPhoneWithCountry,
        countryCode = countryCode,
        countryFlag = countryFlag,
        onCountryCodeClick = onCountryCodeClick
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
    fontFamily: FontFamily = GeistFontFamily
) {
    Text(
        text = text,
        color = color,
        modifier = modifier,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign,
        style = TextStyle(
            fontFamily = fontFamily,
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
    isCircle: Boolean = true,
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
                if (isCircle) {
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

/**
 * Reusable Green Verified Pill Badge
 */
@Composable
fun VerifiedBadge(
    modifier: Modifier = Modifier,
    text: String? = null
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(dimen_12))
            .background(DarkGreen)
            .padding(horizontal = dimen_8, vertical = dimen_4),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.tick_ic),
            contentDescription = null,
            modifier = Modifier.size(dimen_12)
        )
        Spacer(modifier = Modifier.width(dimen_4))
        Textview(
            text = text ?: stringResource(R.string.common_verified),
            size = font_11,
            color = White,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Reusable Profile Photo Avatar with dashed circular border and add photo badge
 */
@Composable
fun ProfileAvatarPicker(
    onAddPhotoClick: () -> Unit,
    modifier: Modifier = Modifier,
    photoUri: String? = null,
    isUploading: Boolean = false,
    // tapping a photo that is set shows it full screen; the + badge still changes it
    onPhotoClick: (() -> Unit)? = null
) {
    val dashColor = appColors.textSecondary.copy(alpha = 0.6f)
    Box(
        modifier = modifier.size(dimen_100),
        contentAlignment = Alignment.Center
    ) {
        // Dashed circle border
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = dashColor,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 2.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            )
        }

        // Inner circle
        Box(
            modifier = Modifier
                .size(dimen_88)
                .glassEffect(CircleShape)
                .clickable { if (photoUri != null && onPhotoClick != null) onPhotoClick() else onAddPhotoClick() },
            contentAlignment = Alignment.Center
        ) {
            if (photoUri != null) {
                ShimmerImage(
                    model = photoUri,
                    contentDescription = stringResource(R.string.profile_photo),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.camera_ic),
                        contentDescription = stringResource(R.string.profile_add_photo),
                        tint = appColors.textPrimary,
                        modifier = Modifier.size(dimen_28)
                    )
                    Spacer(modifier = Modifier.height(dimen_4))
                    Textview(
                        text = stringResource(R.string.profile_add_photo),
                        size = font_11,
                        color = SubtitleGrey,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Upload progress over the photo
        if (isUploading) {
            Box(
                modifier = Modifier
                    .size(dimen_88)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = AmberPrimary,
                    strokeWidth = dimen_2,
                    modifier = Modifier.size(dimen_28)
                )
            }
        }

        // Amber Plus Badge on bottom right
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(dimen_28)
                .clip(CircleShape)
                .background(AmberPrimary)
                .clickable { onAddPhotoClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.profile_add_photo),
                tint = Color.Black,
                modifier = Modifier.size(dimen_18)
            )
        }
    }
}

/**
 * Reusable Glass Role Selection Card
 */
@Composable
fun GlassRoleCard(
    title: String,
    subtitle: String,
    imageRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    activeBorderColor: Color = LightYellow,
    activeBackgroundColor: Color = LightYellow.copy(alpha = 0.10f),
    shape: Shape = RoundedCornerShape(dimen_12)
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .glassEffect(
                shape = shape,
                backgroundColor = if (isSelected) activeBackgroundColor else Color.Unspecified,
                borderColor = if (isSelected) activeBorderColor else Color.Unspecified
            )
            .clickable { onClick() }
            .padding(dimen_12)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Thumbnail
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                modifier = Modifier
                    .size(width = dimen_80, height = dimen_56)
                    .clip(RoundedCornerShape(dimen_10)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(dimen_14))

            // Center Title & Subtitle
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Textview(
                    text = title,
                    size = font_16,
                    color = appColors.textPrimary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(dimen_4))
                Textview(
                    text = subtitle,
                    size = font_12,
                    color = appColors.textSecondary,
                    fontWeight = FontWeight.Normal,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.width(dimen_12))

            // Selection Indicator
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(dimen_24)
                        .background(
                            brush = Brush.linearGradient(
                                listOf(
                                    LightYellow,
                                    DarkYellow
                                )
                            ), shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.common_selected),
                        tint = Black,
                        modifier = Modifier.size(dimen_14)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(dimen_24)
                        .border(
                            width = dimen_1,
                            color = appColors.textHint,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

/**
 * Reusable Circular Glass Back Button
 */
@Composable
fun CommonBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = dimen_40,
    iconSize: Dp = dimen_24,
    backgroundColor: Color = Color.Unspecified,
    borderColor: Color = Color.Unspecified,
    iconColor: Color = Color.Unspecified
) {
    Box(
        modifier = modifier
            .size(size)
            .glassEffect(CircleShape, backgroundColor, borderColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = stringResource(R.string.common_back),
            tint = if (iconColor.isSpecified) iconColor else appColors.textPrimary,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * Reusable Glassmorphic OTP Input Field with glowing active border and cursor
 */
@Composable
fun GlassOtpField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    cells: Int = 6,
    cellWidth: Dp = dimen_46,
    cellHeight: Dp = dimen_56,
    cornerRadius: Dp = dimen_12,
    gap: Dp = dimen_8,
    activeBorderColor: Color = LightYellow,
    inactiveBorderColor: Color = Color.Unspecified,
    glassBgColor: Color = Color.Unspecified,
    textColor: Color = Color.Unspecified,
    cursorColor: Color = LightYellow,
    onDone: () -> Unit = {}
) {
    val focusRequester = remember { FocusRequester() }
    val shape = RoundedCornerShape(cornerRadius)
    val resolvedTextColor = if (textColor.isSpecified) textColor else appColors.textPrimary

    val infiniteTransition = rememberInfiniteTransition(label = "otpCursor")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "otpCursorAlpha"
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    BasicTextField(
        value = value,
        onValueChange = { input ->
            val digitsOnly = input.filter { it.isDigit() }.take(cells)
            onValueChange(digitsOnly)
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        cursorBrush = SolidColor(Color.Transparent),
        modifier = modifier
            .focusRequester(focusRequester)
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyUp && event.key == Key.Backspace && value.isNotEmpty()) {
                    onValueChange(value.dropLast(1))
                    true
                } else false
            },
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.Center) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(gap),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(cells) { index ->
                        val isActive = index == value.length && value.length < cells
                        val charAtCell = value.getOrNull(index)?.toString()

                        val borderColor = when {
                            isActive -> activeBorderColor
                            charAtCell != null -> activeBorderColor.copy(alpha = 0.6f)
                            else -> inactiveBorderColor
                        }

                        Box(
                            modifier = Modifier
                                .size(cellWidth, cellHeight)
                                .glassEffect(
                                    shape = shape,
                                    backgroundColor = glassBgColor,
                                    borderColor = borderColor
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (charAtCell != null) {
                                Textview(
                                    text = charAtCell,
                                    size = font_22,
                                    color = resolvedTextColor,
                                    fontWeight = FontWeight.Bold
                                )
                            } else if (isActive) {
                                Textview(
                                    text = "|",
                                    size = font_20,
                                    color = cursorColor.copy(alpha = cursorAlpha),
                                    fontWeight = FontWeight.Light
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .alpha(0f)
                ) {
                    innerTextField()
                }
            }
        }
    )
}

/**
 * Reusable Glassmorphic OTP Input View
 */
@Composable
fun GlassOtpView(
    modifier: Modifier = Modifier,
    cells: Int = 6,
    value: String = "",
    onValueChanged: (String) -> Unit = {},
    onFilled: (String) -> Unit = {},
    cellWidth: Dp = dimen_46,
    cellHeight: Dp = dimen_56,
    cornerRadius: Dp = dimen_12,
    gap: Dp = dimen_8
) {
    var internalValue by rememberSaveable { mutableStateOf(value) }
    val currentValue = if (value.isNotEmpty()) value else internalValue

    GlassOtpField(
        value = currentValue,
        onValueChange = { digits ->
            internalValue = digits
            onValueChanged(digits)
            if (digits.length == cells) {
                onFilled(digits)
            }
        },
        cells = cells,
        cellWidth = cellWidth,
        cellHeight = cellHeight,
        cornerRadius = cornerRadius,
        gap = gap,
        modifier = modifier
    )
}

/**
 * Reusable Glassmorphic Resend OTP Timer
 */
@Composable
fun GlassResendOtpTimer(
    onResendOtp: () -> Unit,
    modifier: Modifier = Modifier,
    totalSeconds: Int = 45,
    text: String = "Resend OTP in "
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

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isEnabled) {
            Textview(
                text = text,
                size = font_14,
                color = appColors.textPrimary,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.width(dimen_4))
            Textview(
                text = formatAsMMSS(secondsLeft),
                size = font_14,
                color = LightYellow,
                fontWeight = FontWeight.Bold
            )
        } else {
            Textview(
                text = stringResource(R.string.otp_resend),
                size = font_14,
                color = LightYellow,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable {
                    onResendOtp()
                    timerKey++
                }
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
            CranezoLoader()
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
                key = { index, item ->
                    key?.invoke(item) ?: "${item.hashCode()}_$index"
                }) { _, item ->
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
                        CircularProgressIndicator(color = AmberPrimary, strokeWidth = 3.dp, modifier = Modifier.size(27.dp))
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
    title: String,
    subtitle: String,
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
            CranezoLoader()
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
                        CircularProgressIndicator(color = AmberPrimary, strokeWidth = 3.dp, modifier = Modifier.size(27.dp))
                    }
                }
            }
        }
    }
}

/**
 * Reusable frosted glass (glassmorphism) surface for any Composable. Follows light / dark mode.
 *
 * The glass is built from layers so it still reads as glass over any background:
 * 1. a soft shadow (light mode only) that lifts the surface off the screen,
 * 2. a translucent top-to-bottom gradient fill,
 * 3. an optional [backgroundColor] tint on top (selected / highlighted states),
 * 4. a sheen that catches light in the top-left corner,
 * 5. a shining edge: a diagonal gradient stroke that peaks across the middle and fades to
 *    transparent at one corner (dark mode), or a soft white-to-grey edge (light mode).
 *
 * Usage:
 * ```
 * Column(Modifier.glassEffect(RoundedCornerShape(dimen_20)).padding(dimen_16)) { ... }
 *
 * // A tile that sits on top of another glass card: darker, so it reads as inset
 * Modifier.glassEffect(RoundedCornerShape(dimen_14), inset = true)
 *
 * // Highlighted state
 * Modifier.glassEffect(backgroundColor = AmberPrimary.copy(0.12f), borderColor = AmberPrimary)
 * ```
 *
 * @param shape Shape of the glass container.
 * @param backgroundColor Optional tint drawn over the glass fill. Unspecified = plain glass.
 * @param borderColor Optional solid edge color. Unspecified = the shining edge of the theme.
 * @param borderWidth Width of the edge stroke.
 * @param inset Darkens the fill, for tiles nested inside another glass card.
 */
fun Modifier.glassEffect(
    shape: Shape = RoundedCornerShape(dimen_16),
    backgroundColor: Color = Color.Unspecified,
    borderColor: Color = Color.Unspecified,
    borderWidth: Dp = dimen_1,
    inset: Boolean = false
): Modifier = composed {
    val colors = appColors
    // Tiles nested in a card shine softer than the card itself, so the card stays the brighter level
    val shinePeak = colors.glassBorderTop.let { it.copy(alpha = it.alpha * if (inset) 0.5f else 1f) }
    val shine = remember(shinePeak) { shineBrush(shinePeak) }
    // Light mode: a clear grey outline (the white shine edge disappears on white screens)
    val borderBrush = if (borderColor.isSpecified) SolidColor(borderColor)
    // (the shadow only darkens the bottom / right, so the left edge relies on this outline: keep it clearly visible)
    else if (!colors.isDark) SolidColor(Color(0xFF0F172A).copy(alpha = if (inset) 0.20f else 0.26f))
    else Brush.linearGradient(listOf(colors.glassBorderTop, colors.glassBorderBottom))

    this
        .then(
            if (colors.isDark && colors.glassShadow == Color.Transparent) Modifier else Modifier.shadow(
                elevation = dimen_8,
                shape = shape,
                clip = false,
                ambientColor = colors.glassShadow,
                spotColor = colors.glassShadow
            )
        )
        .clip(shape)
        .background(Brush.verticalGradient(listOf(colors.glassTop, colors.glassBottom)))
        .then(if (inset) Modifier.background(colors.glassInset) else Modifier)
        .then(if (backgroundColor.isSpecified) Modifier.background(backgroundColor) else Modifier)
        .drawBehind {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(colors.glassSheen, Color.Transparent),
                    center = Offset.Zero,
                    radius = size.maxDimension * 0.8f
                )
            )
        }
        .border(borderWidth, borderBrush, shape)
}

/**
 * Gradient for the shining edge: runs from the top-right to the bottom-left corner, starts dim,
 * peaks across the middle and fades to transparent. The edge therefore catches the light at the
 * top-left and bottom-right of a card and disappears at the opposite corners.
 */
private fun shineBrush(peak: Color): Brush = object : ShaderBrush() {
    override fun createShader(size: Size): Shader = LinearGradientShader(
        from = Offset(size.width, 0f),
        to = Offset(0f, size.height),
        colors = listOf(
            peak.copy(alpha = peak.alpha * 0.25f),
            peak,
            peak.copy(alpha = peak.alpha * 0.8f),
            Color.Transparent
        )
    )
}

/**
 * Reusable Glassmorphic Box container. See [glassEffect].
 */
@Composable
fun GlassBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(dimen_16),
    backgroundColor: Color = Color.Unspecified,
    borderColor: Color = Color.Unspecified,
    borderWidth: Dp = dimen_1,
    content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.glassEffect(
            shape = shape,
            backgroundColor = backgroundColor,
            borderColor = borderColor,
            borderWidth = borderWidth
        ),
        content = content
    )
}

/**
 * Glass card with inner padding, for grouping content.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(dimen_16),
    contentPadding: Dp = dimen_16,
    // Minimum height of the card type; the card grows with its content, never shrinks below it
    minHeight: Dp = dimen_64,
    backgroundColor: Color = Color.Unspecified,
    borderColor: Color = Color.Unspecified,
    onClick: (() -> Unit)? = null,
    isGlassy: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = if(isGlassy){
            modifier
                .fillMaxWidth()
                .glassEffect(shape, backgroundColor, borderColor)
        }else{
            modifier
        }
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .heightIn(min = minHeight)
            // Padding is at least 16. A group whose rows carry their own padding passes less than 10 on purpose.
            .padding(if (contentPadding < dimen_10) contentPadding else maxOf(contentPadding, dimen_16)),
        content = content
    )
}

/**
 * Screen background that follows light / dark mode.
 *
 * - Dark: [darkImageRes] under a dark top scrim, or a navy gradient with an amber glow when null.
 * - Light: [lightImageRes] if provided, otherwise the dark image under a white veil, otherwise a soft light gradient.
 *
 * Drop dedicated light artwork in by passing [lightImageRes]; nothing else needs to change.
 */
@Composable
fun ScreenBackground(
    modifier: Modifier = Modifier,
    @androidx.annotation.DrawableRes darkImageRes: Int? = null,
    @androidx.annotation.DrawableRes lightImageRes: Int? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = appColors

    // One fixed background for every screen: blue glow top-right, dark centre, amber glow bottom-left.
    // The image parameters are kept so callers still compile; the background is always drawn from tokens.
    // Hero screens (language, onboarding, login, role) keep their full-bleed photo from the design;
    // every other screen uses the glow background below
    val heroRes = if (colors.isDark) darkImageRes else (lightImageRes ?: darkImageRes)
    val useImage = heroRes != null && heroRes != R.drawable.app_bg

    Box(modifier = modifier.fillMaxSize()) {
        if (useImage && heroRes != null) {
            Image(
                painter = painterResource(id = heroRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            val scrim = if (colors.isDark) Brush.verticalGradient(listOf(Black.copy(alpha = 0.60f), Transparent, Transparent))
            else Brush.verticalGradient(
                listOf(colors.imageVeil.copy(alpha = 0.92f), colors.imageVeil.copy(alpha = 0.60f), colors.imageVeil.copy(alpha = 0.82f))
            )
            Box(modifier = Modifier.fillMaxSize().background(scrim))
        }
        if (!useImage) Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.bgBase)
                .drawBehind {
                    // Light mode: champagne gold at the top fading to white, with a soft shine
                    if (!colors.isDark) {
                        drawRect(
                            brush = Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0f to colors.bodyTop,
                                    0.3f to colors.bodyMid,
                                    0.7f to colors.bodyBottom,
                                    1f to colors.bodyBottom
                                )
                            )
                        )
                        // shine: a bright soft glow from the top left corner
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(colors.bodyShine.copy(alpha = 0.65f), Color.Transparent),
                                center = Offset(size.width * 0.1f, size.height * 0.04f),
                                radius = size.width * 0.9f
                            )
                        )
                        return@drawBehind
                    }
                    // Centre darkness (dark mode only): the middle of the screen feels black
                    if (colors.isDark) {
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(colors.bgCenter, colors.bgBase, colors.bgEdge),
                                center = Offset(size.width / 2f, size.height / 2f),
                                radius = size.maxDimension * 0.75f
                            )
                        )
                    }
                    // Top-right blue glow, centred slightly outside the screen
                    drawRect(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0f to colors.glowBlue.copy(alpha = colors.glowBlueAlpha),
                                0.5f to colors.glowBlue.copy(alpha = colors.glowBlueAlpha * 0.35f),
                                1f to colors.glowBlue.copy(alpha = 0f)
                            ),
                            center = Offset(size.width * 1.05f, -size.height * 0.05f),
                            radius = size.width * 0.9f
                        )
                    )
                    // Bottom-left amber glow, a little weaker so text on cards stays readable
                    drawRect(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0f to colors.glowAmber.copy(alpha = colors.glowAmberAlpha),
                                0.5f to colors.glowAmber.copy(alpha = colors.glowAmberAlpha * 0.33f),
                                1f to colors.glowAmber.copy(alpha = 0f)
                            ),
                            center = Offset(-size.width * 0.05f, size.height * 1.05f),
                            radius = size.width * 0.85f
                        )
                    )
                }
        )
        content()
    }
}

/**
 * Reusable Continue / Action button with gradient background, rounded capsule shape, and optional trailing icon.
 *
 * Usage:
 * ```
 * ContinueButton(text = "Continue") {
 *     // click action
 * }
 * ```
 *
 * @param text The text displayed on the button (default "Continue").
 * @param modifier Custom modifier for the outer container.
 * @param enabled Whether the button is clickable.
 * @param showIcon Whether to display the trailing arrow icon (default true).
 * @param icon The trailing vector icon (default ArrowForward).
 * @param textColor Color of the label text (default Black).
 * @param textSize Font size of the label text (default font_16).
 * @param gradientColors List of colors for horizontal gradient.
 * @param height Height of the button (default dimen_52).
 * @param shape Corner radius shape (default RoundedCornerShape(dimen_28)).
 * @param includeBottomPadding Whether to include bottom navigation bar padding (default true).
 * @param onClick Click action lambda.
 */
@Composable
fun ContinueButton(
    text: String = "Continue",
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showIcon: Boolean = true,
    icon: ImageVector = Icons.AutoMirrored.Filled.ArrowForward,
    textColor: Color = Black,
    textSize: TextUnit = font_16,
    gradientColors: List<Color> = listOf(LightYellow, DarkYellow),
    height: Dp = dimen_52,
    shape: Shape = RoundedCornerShape(dimen_34),
    includeBottomPadding: Boolean = true,
    onClick: () -> Unit
) {

    Box(
        modifier = modifier
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(height),
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent
            ),
            contentPadding = PaddingValues(dimen_0)
        ) {
            val backgroundBrush = if (enabled) {
                Brush.horizontalGradient(gradientColors)
            } else {
                Brush.horizontalGradient(gradientColors.map { it.copy(alpha = 0.5f) })
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = backgroundBrush,
                        shape = shape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Textview(
                        text = text,
                        size = textSize,
                        color = if (enabled) textColor else textColor.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )
                    if (showIcon) {
                        Spacer(modifier = Modifier.width(dimen_8))
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (enabled) textColor else textColor.copy(alpha = 0.5f),
                            modifier = Modifier.size(dimen_18)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Reusable Common Gradient Button supporting customizable gradients, left image/icon, and right image/icon.
 *
 * Usage:
 * ```
 * CommonGradientButton(
 *     text = "Get OTP",
 *     gradientColors = listOf(AmberPrimary, AmberSecondary, AmberDark),
 *     rightIcon = Icons.AutoMirrored.Filled.ArrowForward,
 *     onClick = { ... }
 * )
 *
 * CommonGradientButton(
 *     text = "Call for Emergency",
 *     gradientColors = listOf(EmergencyRedStart, EmergencyRedCenter, EmergencyRedEnd),
 *     leftImage = R.drawable.ic_emergency,
 *     textColor = Color.White,
 *     onClick = { ... }
 * )
 * ```
 */
@Composable
fun CommonGradientButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true,
    gradientColors: List<Color> = listOf(AmberPrimary, AmberSecondary),
    textColor: Color = Black,
    textSize: TextUnit = font_16,
    fontWeight: FontWeight = FontWeight.Bold,
    height: Dp = dimen_52,
    shape: Shape = RoundedCornerShape(dimen_34),
    leftImage: Int? = null,
    leftIcon: ImageVector? = null,
    leftIconTint: Color? = textColor,
    rightImage: Int? = null,
    rightIcon: ImageVector? = null,
    rightIconTint: Color? = textColor,
    iconSize: Dp = dimen_20,
    iconSpacing: Dp = dimen_8
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Transparent,
            disabledContainerColor = Transparent
        ),
        contentPadding = PaddingValues(dimen_0)
    ) {
        val backgroundBrush = if (enabled) {
            Brush.horizontalGradient(gradientColors)
        } else {
            Brush.horizontalGradient(gradientColors.map { it.copy(alpha = 0.5f) })
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = backgroundBrush, shape = shape)
                .padding(horizontal = dimen_16),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Show left image if provided, else show left icon if provided
                if (leftImage != null) {
                    if (leftIconTint != null) {
                        Icon(
                            painter = painterResource(id = leftImage),
                            contentDescription = null,
                            tint = leftIconTint,
                            modifier = Modifier.size(iconSize)
                        )
                    } else {
                        Image(
                            painter = painterResource(id = leftImage),
                            contentDescription = null,
                            modifier = Modifier.size(iconSize)
                        )
                    }
                    Spacer(modifier = Modifier.width(iconSpacing))
                } else if (leftIcon != null) {
                    Icon(
                        imageVector = leftIcon,
                        contentDescription = null,
                        tint = leftIconTint ?: textColor,
                        modifier = Modifier.size(iconSize)
                    )
                    Spacer(modifier = Modifier.width(iconSpacing))
                }

                Textview(
                    text = text,
                    size = textSize,
                    color = if (enabled) textColor else textColor.copy(alpha = 0.5f),
                    fontWeight = fontWeight
                )

                // Show right image if provided, else show right icon if provided
                if (rightImage != null) {
                    Spacer(modifier = Modifier.width(iconSpacing))
                    if (rightIconTint != null) {
                        Icon(
                            painter = painterResource(id = rightImage),
                            contentDescription = null,
                            tint = rightIconTint,
                            modifier = Modifier.size(iconSize)
                        )
                    } else {
                        Image(
                            painter = painterResource(id = rightImage),
                            contentDescription = null,
                            modifier = Modifier.size(iconSize)
                        )
                    }
                } else if (rightIcon != null) {
                    Spacer(modifier = Modifier.width(iconSpacing))
                    Icon(
                        imageVector = rightIcon,
                        contentDescription = null,
                        tint = rightIconTint ?: textColor,
                        modifier = Modifier.size(iconSize)
                    )
                }
            }
        }
    }
}

/**
 * Reusable Glassmorphic Phone input field with country code, flag, divider, and labels.
 */
@Composable
fun GlassPhoneField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Mobile Number",
    hint: String = "We'll send you an OTP",
    placeholder: String = "Enter your mobile number",
    countryCode: String = "+91",
    countryFlag: String = "🇮🇳",
    enabled: Boolean = true,
    height: Dp = dimen_52,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(dimen_14),
    onCountryCodeClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Header row: Label on left, Hint on right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = dimen_8),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Textview(
                text = label,
                size = font_14,
                color = White,
                fontWeight = FontWeight.SemiBold
            )
            Textview(
                text = hint,
                size = font_12,
                color = SubtitleGrey,
                fontWeight = FontWeight.Normal
            )
        }

        // Glassy Input Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(shape)
                .background(Color(0x331B2028))
                .border(dimen_1, Color.White.copy(alpha = 0.15f), shape)
                .padding(horizontal = dimen_12),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Country code pill / section
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(dimen_6))
                        .clickable(enabled = onCountryCodeClick != null) { onCountryCodeClick?.invoke() }
                        .padding(vertical = dimen_4, horizontal = dimen_4),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Textview(
                        text = countryFlag,
                        size = font_16
                    )
                    Spacer(modifier = Modifier.width(dimen_4))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(dimen_16)
                    )
                    Spacer(modifier = Modifier.width(dimen_6))
                    Textview(
                        text = countryCode,
                        size = font_14,
                        color = White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(dimen_10))

                // Vertical divider
                Box(
                    modifier = Modifier
                        .width(dimen_1)
                        .height(dimen_24)
                        .background(Color.White.copy(alpha = 0.15f))
                )

                Spacer(modifier = Modifier.width(dimen_12))

                // Editable text field
                BasicTextField(
                    value = value,
                    onValueChange = { input ->
                        if (input.length <= 10 && input.all { char -> char.isDigit() }) {
                            onValueChange(input)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = enabled,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    ),
                    textStyle = TextStyle(
                        fontSize = font_15,
                        color = White,
                        fontWeight = FontWeight.Medium
                    ),
                    cursorBrush = SolidColor(AmberPrimary),
                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Textview(
                                text = placeholder,
                                size = font_14,
                                color = Color(0xFF64748B)
                            )
                        }
                        innerTextField()
                    }
                )
            }
        }
    }
}


