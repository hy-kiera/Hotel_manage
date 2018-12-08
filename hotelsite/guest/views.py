from django.shortcuts import render, redirect, get_object_or_404, HttpResponse
from django.contrib.auth.decorators import login_required
from staff.models import Request_post, Department, Room
from login.models import Guest
from django.contrib.auth.decorators import login_required
from .forms import PostForm

def guest_home(request):
    if request.user.is_staff==True:
        return redirect('staff:staff_home')
    elif request.user.is_active == True :
        return render(request, 'guest/guest_home.html')
    else:
        return render (request, 'guest/guest_home_login.html')

@login_required(login_url='login:sign_in')
def guest_myinfo(request):
    return render(request, 'guest/guest_myinfo.html')

@login_required(login_url='login:sign_in')
def guest_payment(request):
    return render(request, 'guest/payment.html')
  
def guest_room(request):
    if request.user.is_active == True:
        return render(request, 'guest/guest_room.html')
    else:
        return render(request, 'guest/guest_room_login.html')

@login_required(login_url='login:sign_in')
def req(request):
    posts = Request_post.objects.order_by('handle_or_not') 
    return render(request, 'guest/req.html', {'posts': posts})

@login_required(login_url='login:sign_in')
def post_detail(request, pk):
    post = get_object_or_404(Request_post, pk=pk)
    return render(request, 'guest/req_detail.html', {'post':post})

@login_required(login_url='login:sign_in')
def post_new(request):
    if request.method == "POST":
        form = PostForm(request.POST)
        if form.is_valid():
            post = form.save(commit=False)
            post.author=request.user
            post.guest=Guest.objects.get(guest_id=request.user)
            if post.dept != None:
                post.handle_or_not= 2
            post.save()
            return redirect('guest:req')
    else:
        form = PostForm()
    return render(request, 'guest/req_new.html', {'form':form})

def introduce(request):
    if request.user.is_active == True:
        return render(request, 'guest/introduce.html')
    else:
        return render(request, 'guest/introduce_login.html')

@login_required(login_url='login:sign_in')
def myreserv(request):
    return render(request, 'guest/myreserv.html')

@login_required(login_url='login:sign_in')
def post_edit(request, pk):
    post = get_object_or_404(Request_post, pk=pk)
    if request.method == "POST":
        form = PostForm(request.POST, instance=post)
        if form.is_valid():
            form.save()
            return redirect('guest:post_detail', pk=post.pk)
        else:
            return render(request, 'guest/req_new.html', {'form': form})
    else:
        if request.user != post.author:
            return HttpResponse("권한이 없습니다.")
        else:    
            form = PostForm(instance=post)
            return render(request, 'guest/req_new.html', {'form': form})
