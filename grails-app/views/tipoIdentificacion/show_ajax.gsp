
<%@ page import="cratos.sri.TipoIdentificacion" %>

<g:if test="${!tipoIdentificacionInstance}">
    <elm:notFound elem="TipoIdentificacion" genero="o" />
</g:if>
<g:else>

    <g:if test="${tipoIdentificacionInstance?.codigo}">
        <div class="row">
            <div class="col-md-2 text-info">
                Codigo
            </div>
            
            <div class="col-md-3">
                <g:fieldValue bean="${tipoIdentificacionInstance}" field="codigo"/>
            </div>
            
        </div>
    </g:if>
    
    <g:if test="${tipoIdentificacionInstance?.descripcion}">
        <div class="row">
            <div class="col-md-2 text-info">
                Descripcion
            </div>
            
            <div class="col-md-3">
                <g:fieldValue bean="${tipoIdentificacionInstance}" field="descripcion"/>
            </div>
            
        </div>
    </g:if>
    
    <g:if test="${tipoIdentificacionInstance?.codigoSri}">
        <div class="row">
            <div class="col-md-2 text-info">
                Codigo Sri
            </div>
            
            <div class="col-md-3">
                <g:fieldValue bean="${tipoIdentificacionInstance}" field="codigoSri"/>
            </div>
            
        </div>
    </g:if>
    
</g:else>