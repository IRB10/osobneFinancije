package com.diplomski.osobneFinancije.forme

import javax.validation.constraints.NotEmpty

class KategorijaForma {
    @NotEmpty
    var naziv: String = ""

    var opis: String? = ""

    constructor() {}


}