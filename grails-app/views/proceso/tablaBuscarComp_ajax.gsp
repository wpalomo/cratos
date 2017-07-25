


        <table class="table table-bordered table-condensed">
            <tbody>
            <g:each in="${res}" var="comprobante">
                <tr>
                    <td style="width: 150px">${comprobante?.prvenmbr}</td>
                    <td style="width: 350px">${comprobante?.dscr}</td>
                    <td style="width: 150px">${comprobante?.dcmt}</td>
                    <td style="width: 70px; text-align: right">${comprobante?.hber}</td>
                    <td style="width: 70px; text-align: right">${comprobante?.pgdo}</td>
                    <td style="width: 70px; text-align: right">${comprobante?.sldo}</td>
                    <td style="width: 50px; text-align: center">
                        <div class="btn-group">
                            <a href="#" class="btn btn-success btn-sm btnSeleccionarComp"
                               id_cp="${comprobante?.cmpr__id}" dscr="${comprobante?.dscr}" dcmt="${comprobante?.dcmt}"
                               sldo="${comprobante?.sldo}" title="Seleccionar">
                                <i class="fa fa-check"></i>
                            </a>
                        </div>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>

<script type="application/javascript">

    $(".btnSeleccionarComp").click(function () {
        var descripcion = $(this).attr('dscr');
        var saldo = $(this).attr('sldo');
        var id = $(this).attr('id_cp');
        $("#comprobanteDesc").val(descripcion);
//        $("#documento").val($(this).attr('dcmt'));
        $("#comprobanteDoc").val($(this).attr('dcmt'));
        $("#comprobanteSaldo").val(saldo);
        $("#comprobanteSel").val(id);
        if($("#valorPago").val() == '0') {
            $("#valorPago").val(saldo)
        }
        bootbox.hideAll();
    });


</script>