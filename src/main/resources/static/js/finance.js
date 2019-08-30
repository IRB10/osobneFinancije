function myFunction() {
    var popup = document.getElementById("myPopup");
    popup.classList.toggle("show");
}

$(document).ready(function () {

    $("#search-form").submit(function (event) {
        event.preventDefault();
        fire_ajax_submit();
    });
    $("#obligationForm").submit(function (event) {
        event.preventDefault();
        fireUpdate();
    });
});

function fire_ajax_submit() {
    $("#btn-search").prop("disabled", true);
    $.ajax({
            type: "GET",
            url: "/personal-finance/api/finances",
            success: function (data) {
                document.getElementById("added-articles").style.visibility = "visible";
                $("tr:has(td)").remove();

                var entry = data;
                for (var i = 0; i < entry.length; i++) {

                    var amount = $("<td/>");
                    var span1 = $("<span/>");
                    span1.text(entry[i].entryAmount);
                    amount.append(span1);

                    var payday = $("<td/>");
                    var span2 = $("<span/>");
                    span2.text(entry[i].paymentDay);
                    payday.append(span2);

                    var details = $("<td/>");
                    var span3 = $("<span/>");
                    span3.text(entry[i].entryDetails);
                    details.append(span3);

                    var tranFrom = $("<td/>");
                    var span4 = $("<span/>");
                    span4.text(entry[i].transactionFrom);
                    tranFrom.append(span4);

                    var tranTo = $("<td/>");
                    var span5 = $("<span/>");
                    span5.text(entry[i].transactionTo);
                    tranTo.append(span5);
                    var newID = "row" + i;
                    $("#added-articles").append($('<tr class="clickable-row" id="row"/>')
                        .append($('<td/>').html("<span>" + entry[i].name + "</span>"))
                        .append(amount)
                        .append(payday)
                        .append(details)
                        .append(tranFrom)
                        .append(tranTo)
                        .attr("id", newID)
                    );
                    $(document).ready(function ($) {
                        $(".clickable-row").click(function () {
                            var clickedId = $(this).attr("id");
                            var row = document.getElementById(clickedId);
                            for (var j = 0; j < row.cells.length; j++) {
                                var obligationRow = "obligation" + j;
                                document.getElementById(obligationRow).value = row.cells[j].innerText;
                            }
                            document.getElementById("obligation0").disabled = true;
                        });
                    });
                }
            }
        }
    );
}

function fireUpdate() {
    var formData = {
        name: $("#obligation0").val(),
        entryAmount: $("#obligation1").val(),
        paymentDay: $("#obligation2").val(),
        entryDetails: $("#obligation3").val(),
        transactionFrom: $("#obligation4").val(),
        transactionTo: $("#obligation5").val()
    }
    if (formData.name === "" || formData.entryAmount === "" || formData.entryDetails === "" || formData.paymentDay === "" || formData.transactionFrom === "") {
        document.getElementById("postResultDiv").style.visibility = "visible";
        $("#postResultDiv").html("<strong>Please fill in neccesarry fields</strong>");
    }
    else {
        $.ajax({
            type: "POST",
            contentType: "application/json",
            url: "/personal-finance/api/addObligation",
            data: JSON.stringify(formData),
            dataType: 'json',
            success: function (result) {
            }
        });
    }
    resetData();
}

function resetData() {
    $("#obligation0").val("");
    $("#obligation1").val("");
    $("#obligation2").val("");
    $("#obligation3").val("");
    $("#obligation4").val("");
    $("#obligation5").val("");
}