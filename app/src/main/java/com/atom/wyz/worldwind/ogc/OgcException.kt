package com.atom.wyz.worldwind.ogc

import com.atom.wyz.worldwind.ogc.ows.OwsExceptionReport

class OgcException : Exception {
    protected var exceptionReport: OwsExceptionReport? = null
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(cause: Throwable?) : super(cause)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(exceptionReport: OwsExceptionReport? ) : super(exceptionReport?.toPrettyString()){
        this.exceptionReport = exceptionReport;
    }
    constructor(exceptionReport: OwsExceptionReport?, cause:Throwable  ) : super(exceptionReport?.toPrettyString() ,cause){
        this.exceptionReport = exceptionReport;
    }

}