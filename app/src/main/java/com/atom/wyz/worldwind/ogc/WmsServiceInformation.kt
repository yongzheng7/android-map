package com.atom.wyz.worldwind.ogc

import com.atom.wyz.worldwind.util.xml.IntegerModel
import com.atom.wyz.worldwind.util.xml.XmlModel
import javax.xml.namespace.QName

class WmsServiceInformation : XmlModel {
    lateinit var name: QName

    lateinit var title: QName

    lateinit var abstractDescription: QName

    lateinit var fees: QName

    lateinit var accessConstraints: QName

    lateinit var keywordList: QName

    lateinit var keyword: QName

    lateinit var onlineResource: QName

    lateinit var contactInformation: QName

    lateinit var maxWidth: QName

    lateinit var maxHeight: QName

    lateinit var layerLimit: QName

    constructor(namespaceUri: String?) :  super(namespaceUri) {
        initialize()
    }

    private fun initialize() {
        name = QName(this.namespaceUri, "Name")
        title = QName(this.namespaceUri, "Title")
        abstractDescription = QName(this.namespaceUri, "Abstract")
        fees = QName(this.namespaceUri, "Fees")
        accessConstraints =
            QName(this.namespaceUri, "AccessConstraints")
        keywordList = QName(this.namespaceUri, "KeywordList")
        keyword = QName(this.namespaceUri, "Keyword")
        onlineResource = QName(this.namespaceUri, "OnlineResource")
        contactInformation =
            QName(this.namespaceUri, "ContactInformation")
        maxWidth = QName(this.namespaceUri, "MaxWidth")
        maxHeight = QName(this.namespaceUri, "MaxHeight")
        layerLimit = QName(this.namespaceUri, "LayerLimit")
    }

    fun getContactInformation(): WmsContactInformation? {
        return this.getField(contactInformation) as WmsContactInformation?
    }

    protected fun setContactInformation(contactInformation: WmsContactInformation?) {
        this.setField(this.contactInformation , contactInformation)
    }

    fun getOnlineResource(): WmsOnlineResource? {
        return this.getField(onlineResource ) as WmsOnlineResource?
    }

    protected fun setOnlineResource(onlineResource: WmsOnlineResource?) {
        this.setField(this.onlineResource , onlineResource)
    }

    fun getKeywords(): Set<String?>? {
        return (this.getField(keywordList ) as WmsKeywords?)!!.getKeywords()
    }

//    protected void setKeywords(Set<String> keywords) {
//        this.keywords = keywords;
//    }

    //    protected void setKeywords(Set<String> keywords) {
    //        this.keywords = keywords;
    //    }
    fun getAccessConstraints(): String? {
        return getChildCharacterValue(accessConstraints )
    }

    protected fun setAccessConstraints(accessConstraints: String?) {
        setChildCharacterValue(this.accessConstraints , accessConstraints)
    }

    fun getFees(): String? {
        return getChildCharacterValue(fees )
    }

    protected fun setFees(fees: String?) {
        setChildCharacterValue(this.fees , fees)
    }

    fun getServiceAbstract(): String? {
        return getChildCharacterValue(abstractDescription )
    }

    protected fun setServiceAbstract(serviceAbstract: String?) {
        setChildCharacterValue(abstractDescription , serviceAbstract)
    }

    fun getServiceTitle(): String? {
        return getChildCharacterValue(title )
    }

    protected fun setServiceTitle(serviceTitle: String?) {
        setChildCharacterValue(title , serviceTitle)
    }

    fun getServiceName(): String? {
        return getChildCharacterValue(name )
    }

    protected fun setServiceName(serviceName: String?) {
        setChildCharacterValue(name , serviceName)
    }

    fun getMaxWidth(): Int {
        val value = (this.getField(maxWidth) as IntegerModel?)?.getValue()
        return value ?: 0
    }

//    protected void setMaxWidth(int maxWidth) {
//        this.maxWidth = maxWidth;
//    }

    //    protected void setMaxWidth(int maxWidth) {
    //        this.maxWidth = maxWidth;
    //    }
    fun getMaxHeight(): Int {
        val value = (this.getField(maxHeight) as IntegerModel?)?.getValue()
        return value ?: 0
    }

//    protected void setMaxHeight(int maxHeight) {
//        this.maxHeight = maxHeight;
//    }

    //    protected void setMaxHeight(int maxHeight) {
    //        this.maxHeight = maxHeight;
    //    }
    fun getLayerLimit(): Int {
        val value= (this.getField(layerLimit) as IntegerModel?)?.getValue()
        return value ?: 0
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("ServiceName: ")
            .append(if (getServiceName() != null) getServiceName() else "none")
            .append("\n")
        sb.append("ServiceTitle: ")
            .append(if (getServiceTitle() != null) getServiceTitle() else "none")
            .append("\n")
        sb.append("ServiceAbstract: ")
            .append(if (getServiceAbstract() != null) getServiceAbstract() else "none")
            .append(
                "\n"
            )
        sb.append("Fees: ").append(if (getFees() != null) getFees() else "none")
            .append("\n")
        sb.append("AccessConstraints: ").append(
            if (getAccessConstraints() != null) getAccessConstraints() else "none"
        ).append("\n")
        keywordsToString(sb)
        sb.append("OnlineResource: ")
            .append(if (getOnlineResource() != null) getOnlineResource() else "none")
            .append("\n")
        sb.append(if (getContactInformation() != null) getContactInformation() else "none")
            .append("\n")
        sb.append("Max width = ").append(getMaxWidth())
        sb.append(" Max height = ").append(getMaxHeight()).append("\n")
        return sb.toString()
    }

    protected fun keywordsToString(sb: StringBuilder) {
        sb.append("Keywords: ")
        if (getKeywords()!!.size == 0) sb.append(" none") else {
            for (keyword in getKeywords()!!) {
                sb.append(keyword ?: "null").append(", ")
            }
        }
        sb.append("\n")
    }
}