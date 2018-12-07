from django.shortcuts import render, redirect
from django.contrib.auth import login, logout, authenticate
from .forms import LoginForm
from django.contrib.auth.decorators import login_required


def sign_in(request):
    if request.method == "POST":
        username = request.POST['username']
        password = request.POST['password']
        user = authenticate(username=username, password=password)
        print(user)

        if user is not None:
            login(request, user)
            return redirect('../')
        else:
            return render(request, 'sign_in/log_in.html')
    else:
        form = LoginForm()
        return render(request, 'sign_in/log_in.html', {'form':form})

@login_required(login_url='login:sign_in')
def log_out(request):
    logout(request)
    return redirect('login:sign_in')