<%--
  Created by IntelliJ IDEA.
  User: gato
  Date: 25/05/17
  Time: 10:47
--%>

<div class="panel panel-info">
    <div class="panel-heading">
        <h3 class="panel-title">Cambiar la Contabilidad de Trabajo</h3>
    </div>
    <div class="panel-body">
        <g:form controller="contabilidad" action="cambiarContabilidad" name="frmContabilidad">
            <div class="row">
                <div class="col-md-2">
                    <b>Usuario:</b>
                </div>

                <div class="col-md-6">
                    ${usuario?.nombre} ${usuario?.apellido} (${usuario?.login})
                </div>
            </div>

            <div class="row">
                <div class="col-md-2">
                    <b>Contabilidad actual:</b>
                </div>

                <div class="col-md-6">
                    ${contabilidad}
                </div>
            </div>

            <div class="row">
                <div class="col-md-2">
                    <b>Cambiar a contabilidad:</b>
                </div>

                <div class="col-md-8 text-info">
                    <g:select name="contabilidad" from="${contabilidades}" class="form-control text-info" optionKey="id"/>
                </div>
            </div>
        </g:form>
    </div>
</div>
<div class="row">
    <div class="col-md-12 text-center">
        <a href="#" class="btn btn-success" id="btnSave">
            <i class="fa fa-check"></i> Guardar y Retornar a la lista de Procesos
        </a>
    </div>
</div>

<script type="text/javascript">
    $("#btnSave").click(function () {
        $("#frmContabilidad").submit();
    });
    $("#btnCancelar").click(function () {
        window.location.href = "${createLink(controller: 'proceso', action: 'lsta')}";
    });
</script>