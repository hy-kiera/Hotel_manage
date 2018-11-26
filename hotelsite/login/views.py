from django.shortcuts import render, redirect
from .forms import UserForm

from .models import Guest
# 고객 모델로 수정
# 직원은 회사에서 사번을 아이디로 정보를 생성해줌

from django.contrib.auth import login

# from .models import geust, staff 

def sign_in(request):
    return render(request, 'sign_in/index.html')

def sign_up(request):
    if request.method == "POST":
        form = UserForm(request.POST)
        if form.is_valid():
            guest = form.save(commit=False)
            guest.save()
            return redirect('sign_in')

    else:
        form = UserForm()
        return render(request, 'sign_in/sign_up.html', {'form':form})