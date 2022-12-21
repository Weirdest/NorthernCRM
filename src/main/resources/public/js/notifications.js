function displayNoti() {
    const notiDiv = document.getElementById('notiDropdown');

    switch(notiDiv.style.display) {
        case 'block':
            notiDiv.style.display = 'none';
            break;
        case 'none':
        default:
            notiDiv.style.display = 'block';
    }
}

function dismissNoti(form) {
    let id = form.getAttribute('data-id');
    let token = $("meta[name='_csrf']").attr("content");
    let header = `${$("meta[name='_csrf_header']").attr("content")}`;

    $.ajaxSetup({
        beforeSend: function (xhr) {
            xhr.setRequestHeader(header, token);
        }
    });

    $.ajax({
        type: "POST",
        url: "/dismiss/" + id,
        data: {}
    });

    let li_node = form.parentNode;
    li_node.parentNode.removeChild(li_node);

    let bell = document.getElementById('notiBell');

    let currentNotifications = parseInt(bell.innerHTML, 10) -1;

    bell.innerHTML = ` ${currentNotifications}`;

    if (currentNotifications <= 0) {
        bell.style = null;
        displayNoti();
    }
}