package com.cranezo.ui.chooselanguage.model

class ChooseLanguageApiResponse : ArrayList<ChooseLanguageApiResponseItem>()

data class ChooseLanguageApiResponseItem(
    val createdAt: String? = null,
    val dropdownDescription: String? = null,
    val dropdownName: String? = null,
    val dropdownType: Int? = null,
    val dropdownTypeStatus: Int? = null,
    val id: Int? = null,
    val updatedAt: String
)