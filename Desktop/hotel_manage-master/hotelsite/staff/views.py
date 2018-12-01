from django.shortcuts import render, get_object_or_404
from .models import Room
from django.shortcuts import render, redirect
from django.contrib.auth.decorators import login_required

@login_required(login_url='login:sign_in')
def staff_home(request):
    rooms = Room.objects.order_by('room_num')
    return render(request, 'staff/staff_home.html', {'rooms':rooms})