from django.shortcuts import render, redirect, HttpResponse
from django.contrib.auth import login, logout, authenticate
from .forms import LoginForm, UserForm, GuestForm
from django.contrib.auth.decorators import login_required
from django.contrib.auth.models import User
from .models import Guest

def sign_in(request):
    if request.method == "POST":
        username = request.POST['username']
        password = request.POST['password']
        user = authenticate(username=username, password=password)
        print(user)

        if user is not None:
            login(request, user)
            if user.is_staff == True:
                return redirect('staff:staff_home')

            else:
                return redirect('guest:guest_home')
        else:
            return render(request, 'sign_in/log_in.html')
    
    elif request.user.is_active == True:
        if request.user.is_staff == True:
            return redirect('staff:staff_home')

        else:
            return redirect('guest:guest_home')


    else:
        form = LoginForm()
        return render(request, 'sign_in/log_in.html', {'form':form})

@login_required(login_url='login:sign_in')
def log_out(request):
    logout(request)
    return redirect('login:sign_in')


def sign_up(request):
    if request.method == "POST":
        form1 = UserForm(request.POST)
        form2 = GuestForm(request.POST)        
    
        if form1.is_valid():
            guest=form2.save(commit=False)
            guest.date_of_birth=request.POST["date_of_birth"]
            guest.guest_id=request.POST['username']
            guest.save()
            new_user = User.objects.create_user(**form1.cleaned_data)
            return redirect('login:sign_in')
        else:
            return HttpResponse('사용자명이 이미 존재합니다.')

    else:
        form1 = UserForm()
        form2 = GuestForm()
        return render(request, 'sign_in/sign_up.html', {'form1':form1, 'form2':form2})