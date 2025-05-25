//go:build !windows

package windows

import (
	"context"
	"os"
	"os/exec"
)

func (w *WindowsC8Run) OpenBrowser(ctx context.Context, protocol string, port int) error {
	panic("Platform was not built for windows")
}

func (w *WindowsC8Run) ProcessTree(commandPid int) []*os.Process {
	panic("Platform was not built for windows")
}

func (w *WindowsC8Run) VersionCmd(ctx context.Context, javaBinaryPath string) *exec.Cmd {
	panic("Platform was not built for windows")
}

func (w *WindowsC8Run) ElasticsearchCmd(ctx context.Context, elasticsearchVersion string, parentDir string) *exec.Cmd {
	panic("Platform was not built for windows")
}

func (w *WindowsC8Run) ConnectorsCmd(ctx context.Context, javaBinary string, parentDir string, camundaVersion string) *exec.Cmd {
	panic("Platform was not built for windows")
}

func (w *WindowsC8Run) CamundaCmd(ctx context.Context, camundaVersion string, parentDir string, extraArgs string, javaOpts string) *exec.Cmd {
	panic("Platform was not built for windows")
}
