package cratos

class Empresa implements Serializable {
    String email
    Date fechaFin
    Date fechaInicio
    String telefono
    String direccion
    String ruc
    String nombre
    Canton canton
    TipoEmpresa tipoEmpresa
    String sigla
    int numeroComprobanteDiario = 0
    int numeroComprobanteEgreso = 0
    int numeroComprobanteIngreso = 0
    String prefijoDiario
    String prefijoEgreso
    String prefijoIngreso
    // debe fijarse por empresa con atributos adicionales como:
    // si hay o no centros de costos
    // ...
    String ordenCompra = '0'
    String establecimientos
    String tipoEmision

    String contribuyenteEspecial
    String obligadaContabilidad
    String razonSocial
    String ambiente

    String firma
    String clave


    static hasMany = [periodosContables: Contabilidad]

    static mapping = {
        table 'empr'
        cache usage: 'read-write', include: 'non-lazy'
        id column: 'empr__id'
        id generator: 'identity'
        version false
        columns {
            id column: 'empr__id'
            email column: 'emprmail'
            fechaFin column: 'emprfcfn'
            fechaInicio column: 'emprfcin'
            telefono column: 'emprtelf'
            direccion column: 'emprdire'
            ruc column: 'empr_ruc'
            nombre column: 'emprnmbr'
            canton column: 'cntn__id'
            tipoEmpresa column: 'tpem__id'
            sigla column: 'emprsgla'
            numeroComprobanteDiario column: 'emprncmd'
            numeroComprobanteEgreso column: 'emprncme'
            numeroComprobanteIngreso column: 'emprncmi'
            prefijoDiario column: 'emprprdr'
            prefijoEgreso column: 'emprpreg'
            prefijoIngreso column: 'emprprin'

            ordenCompra column: 'emprorcm'

            establecimientos column: 'emprestb'
            tipoEmision column: 'emprtpem'

            obligadaContabilidad column: 'emprcont'
            contribuyenteEspecial column: 'emprctes'
            razonSocial column: 'emprrzsc'
            ambiente column: 'emprambt'
            firma column: 'emprfrma'
            clave column: 'emprclve'
        }
    }
    static constraints = {
        email(size: 1..63, blank: true, nullable: true, email: true, attributes: [title: 'E-mail'])
        fechaFin(blank: true, nullable: true, attributes: [title: 'Fecha de fin'])
        fechaInicio(blank: true, nullable: true, attributes: [title: 'Fecha de inicio'])
        telefono(size: 1..63, blank: true, nullable: true, attributes: [title: 'Teléfono'])
        direccion(size: 1..127, blank: true, nullable: true, attributes: [title: 'Dirección'])
        ruc(size: 1..13, blank: true, nullable: true, attributes: [title: 'RUC'])
        nombre(size: 1..63, blank: true, nullable: true, attributes: [title: 'Nombre'])
        canton(blank: true, nullable: true, attributes: [title: 'Cantón'])
        tipoEmpresa(blank: true, nullable: true, attributes: [title: 'Tipo de Empresa'])
        sigla(blank: true, nullable: true, attributes: [title: 'Sigla'])
        numeroComprobanteDiario(blank: true, nullable: true, size: 1..20)
        numeroComprobanteIngreso(blank: true, nullable: true, size: 1..20)
        numeroComprobanteEgreso(blank: true, nullable: true, size: 1..20)
        prefijoDiario(blank: true, nullable: true, size: 1..20)
        prefijoEgreso(blank: true, nullable: true, size: 1..20)
        prefijoIngreso(blank: true, nullable: true, size: 1..20)
        ordenCompra(blank: true, nullable: true, maxSize: 1)
        establecimientos(blank: false, nullable: false, maxSize: 63)
        tipoEmision(blank: false, nullable: false, maxSize: 1, inList: ['F', 'E'])
        obligadaContabilidad(blank: true, nullable: true, maxSize: 1)
        contribuyenteEspecial(blank: true, nullable: true)
        razonSocial(blank: true, nullable: true)
        ambiente(blank: true, nullable: true)
        firma(blank: true, nullable: true)
        clave(blank: true, nullable: true)
    }

    String toString() {
        return this.nombre
    }

}