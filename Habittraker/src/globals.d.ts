import "react";

declare module "react" {
  interface CSSProperties {
    "--cell-ratio"?: number;
    "--c"?: string;
  }
}
