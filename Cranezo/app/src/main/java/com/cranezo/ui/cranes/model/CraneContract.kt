package com.cranezo.ui.cranes.model

// MVI contract of the crane flow: Add Crane, Crane Submitted, Crane Details, Edit Crane, Upload Document.

/** A file picked by the user and sent to POST /upload. [path] is the server path once uploaded. */
data class UploadItem(
    val id: Long,
    // What to show: the picked uri, or the full url of a file the server already has
    val displayUri: String,
    val path: String? = null,
    val isUploading: Boolean = false,
    val failed: Boolean = false
)

data class DocumentEntry(
    val typeId: Int,
    val upload: UploadItem? = null,
    // yyyy-MM-dd, optional
    val expiry: String = ""
)

/** What the owner has typed in the Add / Edit Crane form. Ids come from the form-options API. */
data class CraneForm(
    val craneTypeId: Int? = null,
    val capacityId: Int? = null,
    val fuelTypeId: Int? = null,
    val modelName: String = "",
    val registration: String = "",
    val location: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val hourlyRate: String = "",
    val minimumHours: String = "",
    val operator: Operator? = null,
    val availability: Int = Availability.AVAILABLE,
    // The first photo is the cover photo
    val photos: List<UploadItem> = emptyList(),
    // One entry per document type id
    val documents: Map<Int, DocumentEntry> = emptyMap()
)

enum class FormMode { ADD, EDIT }

data class CranesUiState(
    val mode: FormMode = FormMode.ADD,
    val options: CraneFormOptionsResponse? = null,
    val optionsLoading: Boolean = false,
    val form: CraneForm = CraneForm(),
    // The crane being edited, to work out what changed
    val original: Crane? = null,
    val isSubmitting: Boolean = false,

    // Crane Details
    val detail: Crane? = null,
    val detailLoading: Boolean = false,
    val detailError: String? = null,

    // Crane Submitted
    val submitted: Crane? = null,
    val submittedMessage: String? = null,

    // Upload Document: only the documents the owner replaced
    val docEdits: Map<Int, DocumentEntry> = emptyMap(),
    val isSavingDocs: Boolean = false,

    // Operator picker
    val operators: List<Operator> = emptyList(),
    val operatorsLoading: Boolean = false,
    val isAddingOperator: Boolean = false
) {
    val isUploading: Boolean
        get() = form.photos.any { it.isUploading } ||
                form.documents.values.any { it.upload?.isUploading == true }

    val isUploadingDocs: Boolean
        get() = docEdits.values.any { it.upload?.isUploading == true }

    /** Document types the owner has to provide. The API decides which they are. */
    val documentTypes: List<FormOption>
        get() = options?.document_types.orEmpty()

    /**
     * Saving this edit sends the crane back to Under Review: it was rejected, or the type, capacity
     * or registration number changed (documents have their own screen).
     */
    val needsReverification: Boolean
        get() {
            val crane = original ?: return false
            if (mode != FormMode.EDIT) return false
            return crane.approval_status == ApprovalStatus.REJECTED ||
                    form.craneTypeId != crane.crane_type ||
                    form.capacityId != crane.capacity ||
                    !form.registration.trim().equals(crane.registration_number.orEmpty().trim(), ignoreCase = true)
        }
}

sealed interface CranesIntent {
    data object LoadOptions : CranesIntent
    data class RetryOptionList(val type: Int) : CranesIntent

    // Add / Edit form
    data object StartAdd : CranesIntent
    data class StartEdit(val crane: Crane) : CranesIntent
    data class CraneTypeChanged(val id: Int) : CranesIntent
    data class CapacityChanged(val id: Int) : CranesIntent
    data class FuelTypeChanged(val id: Int) : CranesIntent
    data class ModelChanged(val value: String) : CranesIntent
    data class RegistrationChanged(val value: String) : CranesIntent
    data class LocationChanged(val value: String) : CranesIntent
    data class GpsLocationFound(val location: String?, val latitude: Double, val longitude: Double) : CranesIntent
    data class HourlyRateChanged(val value: String) : CranesIntent
    data class MinimumHoursChanged(val value: String) : CranesIntent
    data class OperatorSelected(val operator: Operator?) : CranesIntent
    data class AvailabilityChanged(val value: Int) : CranesIntent

    data class PhotoPicked(val uri: String) : CranesIntent
    data class PhotoRemoved(val id: Long) : CranesIntent
    data class PhotoRetry(val id: Long) : CranesIntent
    data class DocumentPicked(val typeId: Int, val uri: String) : CranesIntent
    data class DocumentRemoved(val typeId: Int) : CranesIntent
    data class DocumentRetry(val typeId: Int) : CranesIntent
    data class DocumentExpiryChanged(val typeId: Int, val date: String) : CranesIntent

    data object SubmitCrane : CranesIntent
    data object SaveEdit : CranesIntent

    // Details
    data class LoadDetail(val craneId: String) : CranesIntent

    // Upload Document
    data object StartDocEdit : CranesIntent
    data class DocEditPicked(val typeId: Int, val uri: String) : CranesIntent
    data class DocEditRetry(val typeId: Int) : CranesIntent
    data class DocEditExpiryChanged(val typeId: Int, val date: String) : CranesIntent
    data object SaveDocuments : CranesIntent

    // Operators
    data class SearchOperators(val query: String) : CranesIntent
    data class AddOperator(val fullName: String, val mobile: String, val email: String) : CranesIntent
}

sealed interface CranesEffect {
    /** Crane created: show the Crane Submitted screen. */
    data object NavigateToSubmitted : CranesEffect

    /** Edit or documents saved: back to Crane Details. */
    data object NavigateBackToDetails : CranesEffect

    /** The operator was added and selected: close the picker. */
    data object OperatorAdded : CranesEffect
}
