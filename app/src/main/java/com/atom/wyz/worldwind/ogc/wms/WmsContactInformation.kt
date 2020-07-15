package com.atom.wyz.worldwind.ogc.wms

import com.atom.wyz.worldwind.util.xml.XmlModel
import javax.xml.namespace.QName

class WmsContactInformation(namespaceUri: String?) : XmlModel(namespaceUri) {

    lateinit var contactPosition: QName

    lateinit var contactVoiceTelephone: QName

    lateinit var contactFacsimileTelephone: QName

    lateinit var contactElectronicMailAddress: QName

    lateinit var contactPersonPrimary: QName

    lateinit var contactAddress: QName

    lateinit var contactPerson: QName

    lateinit var contactOrganization: QName

    init {
        initialize()
    }

    private fun initialize() {
        contactPosition =
            QName(this.namespaceUri, "ContactPosition")
        contactVoiceTelephone =
            QName(this.namespaceUri, "ContactVoiceTelephone")
        contactFacsimileTelephone =
            QName(this.namespaceUri, "ContactFacsimileTelephone")
        contactElectronicMailAddress =
            QName(this.namespaceUri, "ContactElectronicMailAddress")
        contactPersonPrimary =
            QName(this.namespaceUri, "ContactPersonPrimary")
        contactAddress =
            QName(this.namespaceUri, "ContactAddress")
        contactPerson =
            QName(this.namespaceUri, "ContactPerson")
        contactOrganization =
            QName(this.namespaceUri, "ContactOrganization")
    }


    //    @Override
    //    protected void doParseEventContent(XmlEventParserContext ctx, XmlEvent event, Object... args)
    //        throws XMLStreamException
    //    {
    //        if (ctx.isStartElement(event, contactPosition))
    //        {
    //            this.setPosition(ctx.getStringParser().parseString(ctx, event));
    //        }
    //        else if (ctx.isStartElement(event, contactVoiceTelephone))
    //        {
    //            this.setVoiceTelephone(ctx.getStringParser().parseString(ctx, event));
    //        }
    //        else if (ctx.isStartElement(event, contactFacsimileTelephone))
    //        {
    //            this.setFacsimileTelephone(ctx.getStringParser().parseString(ctx, event));
    //        }
    //        else if (ctx.isStartElement(event, contactElectronicMailAddress))
    //        {
    //            this.setElectronicMailAddress(ctx.getStringParser().parseString(ctx, event));
    //        }
    //        else if (ctx.isStartElement(event, contactPersonPrimary))
    //        {
    //            String[] sa = this.parseContactPersonPrimary(ctx, event);
    //            this.setPersonPrimary(sa[0]);
    //            this.setOrganization(sa[1]);
    //        }
    //        else if (ctx.isStartElement(event, CONTACT_ADDRESS))
    //        {
    //            XmlEventParser parser = this.allocate(ctx, event);
    //            if (parser != null)
    //            {
    //                Object o = parser.parse(ctx, event, args);
    //                if (o != null && o instanceof WmsAddress)
    //                    this.setContactAddress((WmsAddress) o);
    //            }
    //        }
    //    }
    //
    //    protected String[] parseContactPersonPrimary(XmlEventParserContext ctx, XmlEvent cppEvent) throws XMLStreamException
    //    {
    //        String[] items = new String[2];
    //
    //        for (XmlEvent event = ctx.nextEvent(); event != null; event = ctx.nextEvent())
    //        {
    //            if (ctx.isEndElement(event, cppEvent))
    //                return items;
    //
    //            if (ctx.isStartElement(event, contactPerson))
    //            {
    //                items[0] = ctx.getStringParser().parseString(ctx, event);
    //            }
    //            else if (ctx.isStartElement(event, contactOrganization))
    //            {
    //                items[1] = ctx.getStringParser().parseString(ctx, event);
    //            }
    //        }
    //
    //        return null;
    //    }
    fun getPersonPrimary(): String? {
        val personPrimary = this.getField(contactPersonPrimary) as XmlModel?
        return (personPrimary!!.getField(contactPerson) as XmlModel?)!!.getField(CHARACTERS_CONTENT).toString()
    }

    protected fun setPersonPrimary(personPrimary: String?) {
        var currentPersonPrimary = this.getField(contactPersonPrimary) as XmlModel?
        if (currentPersonPrimary == null) {
            currentPersonPrimary = XmlModel(this.namespaceUri)
            this.setField(contactPersonPrimary, currentPersonPrimary)
        }
        val person = XmlModel(this.namespaceUri)
        person.setField(CHARACTERS_CONTENT, personPrimary)
        currentPersonPrimary.setField(contactPerson, person)
    }

    fun getOrganization(): String? {
        val personPrimary = this.getField(contactPersonPrimary) as XmlModel?
        return (personPrimary!!.getField(contactOrganization) as XmlModel?)!!.getField(
            CHARACTERS_CONTENT
        )
            .toString()
    }

    protected fun setOrganization(organization: String?) {
        var currentOrganization = this.getField(contactPersonPrimary) as XmlModel?
        if (currentOrganization == null) {
            currentOrganization = XmlModel(this.namespaceUri)
            this.setField(contactPersonPrimary, currentOrganization)
        }
        val org = XmlModel(this.namespaceUri)
        org.setField(CHARACTERS_CONTENT, organization)
        currentOrganization.setField(contactPerson, org)
    }

    fun getPosition(): String? {
        return getChildCharacterValue(contactPosition)
    }

    protected fun setPosition(position: String?) {
        setChildCharacterValue(contactPosition, position)
    }

    fun getVoiceTelephone(): String? {
        return getChildCharacterValue(contactVoiceTelephone)
    }

    protected fun setVoiceTelephone(voiceTelephone: String?) {
        setChildCharacterValue(contactVoiceTelephone, voiceTelephone)
    }

    fun getFacsimileTelephone(): String? {
        return getChildCharacterValue(contactFacsimileTelephone)
    }

    protected fun setFacsimileTelephone(facsimileTelephone: String?) {
        setChildCharacterValue(contactFacsimileTelephone, facsimileTelephone)
    }

    fun getElectronicMailAddress(): String? {
        return getChildCharacterValue(contactElectronicMailAddress)
    }

    protected fun setElectronicMailAddress(electronicMailAddress: String?) {
        setChildCharacterValue(contactElectronicMailAddress, electronicMailAddress)
    }

    fun getContactAddress(): WmsAddress? {
        return this.getField(contactAddress) as WmsAddress?
    }

    protected fun setContactAddress(contactAddress: WmsAddress?) {
        this.setField(this.contactAddress, contactAddress)
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("PersonPrimary: ")
            .append(if (getPersonPrimary() != null) getPersonPrimary() else "none")
            .append("\n")
        sb.append("Organization: ")
            .append(if (getOrganization() != null) getOrganization() else "none")
            .append("\n")
        sb.append("Position: ")
            .append(if (getPosition() != null) getPosition() else "none").append("\n")
        sb.append("VoiceTelephone: ")
            .append(if (getVoiceTelephone() != null) getVoiceTelephone() else "none")
            .append("\n")
        sb.append("FacsimileTelephone: ").append(
            if (getFacsimileTelephone() != null) getFacsimileTelephone() else "none"
        ).append("\n")
        sb.append("ElectronicMailAddress: ").append(
            if (getElectronicMailAddress() != null) getElectronicMailAddress() else "none"
        ).append("\n")
        sb.append(contactAddress)
        return sb.toString()
    }
}