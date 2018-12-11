from django.shortcuts import render, get_object_or_404, redirect, HttpResponse
from .models import Room, Staff, Request_post, Department, Reservation
from django.contrib.auth.models import User
from django.contrib.auth.decorators import login_required
from django.contrib.admin.views.decorators import staff_member_required
from .forms import PostForm
from django.db.models import F
import json 
from django.http import JsonResponse


@staff_member_required
def staff_home(request):
    super_staff = Staff.objects.get(user__username='doh')
    print(super_staff)
    return render(request, 'staff/staff_home.html', {'super_staff':super_staff})

@staff_member_required
def room(request):
    """ request.metho == POST """
    if request.method == 'POST':
        body = json.loads(request.body)
        # print('POST select:', body['select'])
        floor = body['select']
        rooms = Room.objects.filter(room_floor=floor).order_by('room_num').values()
        # print(rooms.query)
        rooms = list(rooms)
        # print(rooms)
        return JsonResponse({
            'rooms': rooms
        })
    else:
        rooms = Room.objects.filter(room_floor=1).order_by('room_num')
        return render(request, 'staff/room.html', {'rooms':rooms})

@staff_member_required
def guest_req(request): 
    posts = Request_post.objects.order_by('handle_or_not') 
    return render(request, 'staff/guest_req.html', {'posts': posts})

@staff_member_required
def post_new(request):
    if request.method == "POST":
        form = PostForm(request.POST)
        if form.is_valid():
            post = form.save(commit=False)
            post.author=request.user
            if post.dept != None:
                post.handle_or_not= 2
            post.save()
            return redirect('staff:guest_req')
    else:
        form = PostForm()
    return render(request, 'staff/req_new.html', {'form':form})

@staff_member_required
def post_detail(request, pk):
    post = get_object_or_404(Request_post, pk=pk)

    if request.method == "POST":
        if request.POST['handle']=='0':
            post.handle_or_not =3
            post.save()
            return redirect('staff:guest_req')
      
        elif request.POST['handle']=='1':
            if request.POST['dept'] != '':
                dept=Department.objects.get(id=request.POST['dept'])
                post.dept=dept
                post.save()
                return redirect('staff:post_detail', pk=pk)
            else:
                post.dept = None
                post.handle_or_not = 1
                post.save()
                return redirect('staff:guest_req')
        
        else:
            user=User.objects.get(username=request.POST['staff'])
            post_staff=Staff.objects.get(pk=user)
            post.staff=post_staff
            post.handle_or_not = 2
            post.save()
            return redirect('staff:guest_req')
    else:
        form = PostForm(instance=post)
        if post.dept is not None:
            staffs = Staff.objects.filter(dept=post.dept).order_by('status')
            return render(request, 'staff/req_detail.html', {'post':post, 'form':form, 'staffs':staffs})
        else:
            return render(request, 'staff/req_detail.html', {'post':post, 'form':form})


@staff_member_required
def myinfo(request):
    staff = Staff.objects.get(pk=request.user)
    return render(request, 'staff/myinfo.html', {'staff':staff})

@staff_member_required
def post_edit(request, pk):
    post = get_object_or_404(Request_post, pk=pk)
    if request.method == "POST":
        form = PostForm(request.POST, instance=post)
        if form.is_valid():
            form.save()
            return redirect('post_detail', pk=post.pk)
        else:
            return render(request, 'guest/req_new.html', {'form': form})
    else:
        if request.user != post.author:
            return HttpResponse("권한이 없습니다.")
        else:    
            form = PostForm(instance=post)
            return render(request, 'guest/req_new.html', {'form': form})

@staff_member_required
def staffs_info(request):
    """ request.metho == POST """
    if request.method == 'POST':
        body = json.loads(request.body)
        print('POST select:', body['select'])
        dept = body['select']
        if dept == '1':
            dept = 'BACK_OFFICE'
        elif dept == '2':
            dept = 'SALES_MARKETING'
        elif dept == '3':
            dept = 'FRONT_OFFICE'
        elif dept == '4':
            dept = 'HOUSE_KEEPING'
        elif dept == '5':
            dept = 'FITNESS'
        elif dept == '6':
            dept = 'FOOD_BEVERAGE'
        else:
            dept = 'FINANCE'
        staffs = Staff.objects.filter(dept__name=dept).values()
        # print(staffs.query)
        staffs = list(staffs)
        # print(staffs)
        return JsonResponse({
            'staffs': staffs
        })
    else:
        staffs = Staff.objects.filter(dept__name='BACK_OFFICE')
        return render(request, 'staff/staffs_info.html', {'staffs':staffs})

    
@login_required(login_url='login:sign_in')
def reserve_status(request):
    if request.method == "POST":
        reserve = Reservation.objects.get(reserve_num=request.POST['reservation'])
        return render(request, 'staff/reserve_status.html', {'reserve' : reserve})
    else:
        return render(request, 'staff/reserve_status.html')
 
