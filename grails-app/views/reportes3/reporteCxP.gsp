<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Auxiliar por clientes</title>

    <style type="text/css">
    @page {
        size   : 29.7cm 21cm;  /*width height */
        margin : 2cm;
    }

    html {
        font-family : Verdana, Arial, sans-serif;
        font-size   : 11px;
    }

    .hoja {
        width : 25cm;
        /*background : #d8f0fa;*/
    }

    h1, h2, h3 {
        text-align : center;
    }

    h1 {
        font-size  : 16px;
        margin-top : 30px;
    }

    table {
        border-collapse : collapse;
        width           : 100%;
    }

    th, td {
        vertical-align : middle;
    }

    th {
        background : #bbb;
    }

    .even {
        background : #ddd;
    }

    .odd {
        background : #efefef;
    }

    .cliente th {
        background : #D1D1D1 !important;
    }

    .right {
        text-align : right;
    }

    .errorReporte {
        font-size   : 20px;
        font-weight : bold;
        padding     : 20px;
        text-align  : center;
        border      : solid 1px #a31b27;
        color       : #a31b27;
        background  : #ffcecf;
    }
    .titulo{
       font-size: 14px;
        font-weight: bold;
    }
    </style>

</head>

<body>
<div class="hoja">
    <div class="titulo">
        <p>${empresa}</p>
        <p>
            Reporte de cuentas por pagar del ${fechaInicio.format("dd-MM-yyyy")} hasta ${fechaFin.format("dd-MM-yyyy")}
        </p>
    </div>
    <div style="width: 95%;margin-top: 20px;">
        <table border="1">
            <thead>
            <tr>
                <th>Fecha</th>
                <th>Documento</th>
                <th>Proveedor</th>
                <th>Concepto</th>
                <th>Monto</th>

            </tr>
            </thead>
            <tbody>
            <g:set var="total" value="${0}"></g:set>
            <g:each in="${cxp}" var="aux">
                <tr>
                    <td style="text-align: left">${aux.fechaPago.format("dd-MM-yyyy")}</td>
                    <td style="text-align: left">${aux.asiento.comprobante.proceso.documento}</td>
                    <td style="text-align: left">${aux.asiento.comprobante.proceso.proveedor}</td>
                    <td style="text-align: left">${aux.descripcion}</td>
                    <td style="text-align: right">${valores[aux.id]}</td>
                     <g:set var="total" value="${total.toDouble()+valores[aux.id]}"></g:set>
                </tr>
            </g:each>
            <tr>
                <td style="text-align: left;font-weight: bold" colspan="4">TOTAL A PAGAR</td>
                <td style="text-align: right">${total.round(2)}</td>
            </tr>
            </tbody>
        </table>


    </div>

</div>
</body>
</html>