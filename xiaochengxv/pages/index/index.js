// pages/index/index.js
Page({
  data: {
    userInfo: {
      nickName: '',
      gender: '',
      avatarUrl: '',
    },
    loginCode:'',
    shopStatus:''
  },

  //请求用户信息
  getUserProfile() {
    wx.getUserProfile({
      desc: '用于展示信息',
      success: (res) => {
        console.log(res)
        this.setData({
          userInfo: res.userInfo
        })
      },
      fail:(error) => {
        console.log(error);
      }
    })
  },

  // 微信登陆，获取微信用户授权码
  wxLogin(){
    wx.login({
      success:(res)=>{
        console.log(res);
        this.setData({
          code:res.code
        })
      }
    })
  },

  //发送请求
  sendRequest(){
    wx.request({
      url:'http://localhost:8080/user/shop/status',
      method:'GET',
      success:(res)=>{
        console.log(res.data.data);
        this.setData({
            shopStatus:res.data.data
        })
      }
    })
  }
})