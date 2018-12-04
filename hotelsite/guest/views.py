from django.shortcuts import render
from django.contrib.auth.decorators import login_required


def guest_home(request):
    return render(request, 'guest/guest_home.html')

def guest_myinfo(request):
    return render(request, 'guest/guest_myinfo.html')

def guest_payment(request):
    return render(request, 'guest/payment.html')

def guest_room(request):
    return render(request, 'guest/guest_room.html')
