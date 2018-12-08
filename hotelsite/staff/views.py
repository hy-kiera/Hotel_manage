from django.shortcuts import render, get_object_or_404, redirect, HttpResponse
from .models import Room, Staff, Request_post, Department
from django.contrib.auth.decorators import login_required
from django.contrib.admin.views.decorators import staff_member_required
from .forms import PostForm
from django.db.models import F
import json 
from django.http import JsonResponse


@staff_member_required
def staff_home(request):
    print(request.GET.get('floor'))
    rooms = Room.objects.order_by('room_num')
    return render(request, 'staff/staff_home.html', {'rooms':rooms})

@login_required(login_url='login:sign_in')
def room(request):
    """ request.metho == POST """
    if request.method == 'POST':
        body = json.loads(request.body)
        # print('POST select:', body['select'])
        floor = body['select']
        rooms = Room.objects.filter(room_floor=floor).order_by('room_num').values()
        rooms = list(rooms)
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
      
        else:
            if request.POST['dept'] != '':
                dept = Department.objects.get(id=request.POST['dept'])
                post.dept = dept
                post.handle_or_not = 2
            else:
                post.dept = None
                post.handle_or_not = 1
        post.save()
        return redirect('staff:guest_req')
    else:
        form = PostForm(instance=post)
        return render(request, 'staff/req_detail.html', {'post':post, 'form':form})

@staff_member_required
def myinfo(request):
    staff = Staff.objects.filter(pk=request.user)
    print(staff.query)
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