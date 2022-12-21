$(document).ready(function() {
    $('#password_form').submit(function(e){
        var form = this;
        e.preventDefault();
        // Check Passwords are the same
        if( $('#pswd1').val()===$('#pswd2').val() ) {
            // Submit Form
            form.submit();
        } else {
            // Complain bitterly
            alert('Password Mismatch');
            return false;
        }
    });
});