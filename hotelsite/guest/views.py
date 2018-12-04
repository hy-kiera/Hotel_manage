from django.shortcuts import render, redirect, get_object_or_404
from django.contrib.auth.decorators import login_required
from staff.models import Request_post, Department, Room
from django.contrib.auth.decorators import login_required
from .forms import PostForm

@login_required(login_url='login:sign_in')
def guest_home(request):
    return render(request, 'guest/guest_home.html')

@login_required(login_url='login:sign_in')
def guest_myinfo(request):
    return render(request, 'guest/guest_myinfo.html')

@login_required(login_url='login:sign_in')
def guest_payment(request):
    return render(request, 'guest/payment.html')
  
@login_required(login_url='login:sign_in')
def guest_room(request):
    return render(request, 'guest/guest_room.html')

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
            if post.dept != None:
                post.handle_or_not= 2
            post.save()
            return redirect('req')
    else:
        form = PostForm()
    return render(request, 'guest/req_new.html', {'form':form})