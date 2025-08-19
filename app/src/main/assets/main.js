function sayHello() {
    alert('你好，这是本地JS代码！')
}

// 文件选择功能
function selectFile() {
    const input = document.createElement('input')
    input.type = 'file'
    input.multiple = true
    input.accept = '*/*'
    
    input.onchange = function(e) {
        const files = e.target.files
        console.log('选择的文件:', files)
        
        for (let i = 0; i < files.length; i++) {
            const file = files[i]
            console.log(`文件 ${i}: ${file.name}, 大小: ${file.size} bytes, 类型: ${file.type}`)
            
            // 读取文件内容
            const reader = new FileReader()
            reader.onload = function(event) {
                console.log(`文件 ${file.name} 内容:`, event.target.result)
                
                // 显示文件信息
                const fileInfo = document.createElement('div')
                fileInfo.innerHTML = `
                    <h4>文件信息：</h4>
                    <p>名称：${file.name}</p>
                    <p>大小：${file.size} bytes</p>
                    <p>类型：${file.type}</p>
                    <p>最后修改：${new Date(file.lastModified)}</p>
                `
                document.body.appendChild(fileInfo)
            }
            
            // 根据文件类型读取
            if (file.type.startsWith('text/') || file.name.endsWith('.txt') || file.name.endsWith('.js') || file.name.endsWith('.html')) {
                reader.readAsText(file)
            } else {
                reader.readAsArrayBuffer(file)
            }
        }
    }
    
    input.click()
}

// Android文件系统访问功能
function accessAndroidFiles() {
    if (typeof AndroidFileAccess !== 'undefined') {
        const info = document.createElement('div')
        info.innerHTML = `
            <h3>Android 文件系统信息：</h3>
            <p>外部存储路径：${AndroidFileAccess.getExternalStoragePath()}</p>
            <p>应用文件路径：${AndroidFileAccess.getAppFilesPath()}</p>
            <p>应用外部文件路径：${AndroidFileAccess.getAppExternalFilesPath()}</p>
        `
        
        // 列出一些目录的文件
        const externalPath = AndroidFileAccess.getExternalStoragePath()
        const externalFiles = AndroidFileAccess.listFiles(externalPath + '/Download')
        info.innerHTML += `<p>Download目录文件：${externalFiles}</p>`
        
        document.body.appendChild(info)
        
        AndroidFileAccess.showToast('文件系统信息已显示')
    } else {
        alert('Android文件访问接口不可用')
    }
}

// 读取特定文件
function readSpecificFile() {
    if (typeof AndroidFileAccess !== 'undefined') {
        const path = prompt('请输入文件路径:')
        if (path) {
            const exists = AndroidFileAccess.fileExists(path)
            if (exists) {
                const content = AndroidFileAccess.readFile(path)
                const fileContent = document.createElement('div')
                fileContent.innerHTML = `
                    <h4>文件内容：</h4>
                    <pre>${content}</pre>
                `
                document.body.appendChild(fileContent)
            } else {
                AndroidFileAccess.showToast('文件不存在：' + path)
            }
        }
    } else {
        alert('Android文件访问接口不可用')
    }
}


console.log('main.js---')

window.addEventListener('DOMContentLoaded', () => {
    console.log('DOMContentLoaded-----')
    const getdata = document.querySelector('#getdata')
    getdata.addEventListener('click', (e) => {
        e.preventDefault()
        console.log('getdata')
        fetch('https://api.github.com/users/octocat')
            .then((response) => response.json())
            .then((data) => {
                console.log('data', data)
                const greetMsgEl = document.querySelector('#greet-msg')
                greetMsgEl.textContent = data.login
            })
            .catch((error) => {
                console.error('error', error)
            })
    })
})
