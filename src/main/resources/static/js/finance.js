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
            url: "/api/finances",
            success: function (data) {
                document.getElementById("added-articles").style.visibility = "visible";
                $("tr:has(td)").remove();

                var entry = data;
                for (var i = 0; i < entry.length; i++) {

                    var amount = $("<td/>");
                    var span1 = $("<span/>");
                    span1.text(entry[i].naziv);
                    amount.append(span1);

                    var payday = $("<td/>");
                    var span2 = $("<span/>");
                    span2.text(entry[i].danPlacanja);
                    payday.append(span2);

                    var details = $("<td/>");
                    var span3 = $("<span/>");
                    span3.text(entry[i].opis);
                    details.append(span3);

                    var tranFrom = $("<td/>");
                    var span4 = $("<span/>");
                    span4.text(entry[i].transakcijaOd);
                    tranFrom.append(span4);

                    var tranTo = $("<td/>");
                    var span5 = $("<span/>");
                    span5.text(entry[i].transakcijaPrema);
                    tranTo.append(span5);
                    var newID = "row" + i;
                    $("#added-articles").append($('<tr class="clickable-row" id="row"/>')
                        .append($('<td/>').html("<span>" + entry[i].naziv + "</span>"))
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
        naziv: $("#obligation0").val(),
        vrijednost: $("#obligation1").val(),
        danPlacanja: $("#obligation2").val(),
        opis: $("#obligation3").val(),
        transakcijaOd: $("#obligation4").val(),
        transakcijaPrema: $("#obligation5").val()
    };
    if (formData.naziv === "" || formData.vrijednost === "" || formData.opis === "" || formData.danPlacanja === "" || formData.transakcijaOd === "") {
        document.getElementById("postResultDiv").style.visibility = "visible";
        $("#postResultDiv").html("<strong>Please fill in neccesarry fields</strong>");
    }
    else {
        $.ajax({
            type: "POST",
            contentType: "application/json",
            url: "/api/addObligation",
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